package com.smecs.security;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.smecs.annotation.RequireOwnership;
import com.smecs.dto.ResponseDTO;
import com.smecs.exception.ForbiddenException;
import com.smecs.exception.ResourceNotFoundException;
import com.smecs.validation.OwnershipValidator;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.jspecify.annotations.NonNull;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;
import org.springframework.web.method.HandlerMethod;
import org.springframework.web.servlet.HandlerExecutionChain;
import org.springframework.web.servlet.mvc.method.annotation.RequestMappingHandlerMapping;

import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * Servlet-layer ownership filter.
 *
 * <p>Runs in the {@link org.springframework.security.web.SecurityFilterChain} immediately
 * after {@link JwtAuthenticationFilter} has validated the JWT and populated the
 * {@link org.springframework.security.core.context.SecurityContext}.
 *
 * <p>For every incoming request the filter:
 * <ol>
 *   <li>Resolves the target {@link HandlerMethod} via Spring MVC's
 *       {@link RequestMappingHandlerMapping}.</li>
 *   <li>Checks whether the method (or its class) is annotated with
 *       {@link RequireOwnership}.</li>
 *   <li>Reads the authenticated {@link SmecsUserPrincipal} from the
 *       {@code SecurityContext} and skips the check for admins.</li>
 *   <li>Extracts the resource ID from the path/query/body arguments declared in
 *       the annotation and delegates to the matching {@link OwnershipValidator}.</li>
 * </ol>
 *
 * <p>On failure the filter short-circuits the chain and writes a JSON
 * {@link ResponseDTO} body with the appropriate HTTP status (401 / 403 / 404).
 */
@Component
public class OwnershipFilter extends OncePerRequestFilter {

    private final RequestMappingHandlerMapping handlerMapping;
    private final Map<String, OwnershipValidator> validators;
    private final ObjectMapper objectMapper = new ObjectMapper();

    @Autowired
    public OwnershipFilter(RequestMappingHandlerMapping handlerMapping,
                           List<OwnershipValidator> validatorList) {
        this.handlerMapping = handlerMapping;
        this.validators = validatorList.stream()
                .collect(Collectors.toMap(
                        OwnershipValidator::getResourceType,
                        Function.identity()
                ));
    }

    @Override
    protected void doFilterInternal(@NonNull HttpServletRequest request,
                                    @NonNull HttpServletResponse response,
                                    @NonNull FilterChain filterChain)
            throws ServletException, IOException {

        // ── 1. Resolve the handler method ────────────────────────────────────────
        HandlerMethod handlerMethod = resolveHandlerMethod(request);
        if (handlerMethod == null) {
            // No mapped handler (static resource, 404, etc.) — let the chain decide
            filterChain.doFilter(request, response);
            return;
        }

        // ── 2. Check for @RequireOwnership on the method ─────────────────────────
        RequireOwnership annotation = handlerMethod.getMethodAnnotation(RequireOwnership.class);
        if (annotation == null) {
            filterChain.doFilter(request, response);
            return;
        }

        // ── 3. Read authenticated principal ──────────────────────────────────────
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null || !auth.isAuthenticated()
                || !(auth.getPrincipal() instanceof SmecsUserPrincipal principal)) {
            writeError(response, HttpStatus.UNAUTHORIZED, "Authentication required for ownership validation");
            return;
        }

        Long userId = principal.getUserId();
        if (userId == null) {
            writeError(response, HttpStatus.UNAUTHORIZED, "Authentication required for ownership validation");
            return;
        }

        // Admins bypass ownership checks
        if ("admin".equalsIgnoreCase(principal.getRole())) {
            filterChain.doFilter(request, response);
            return;
        }

        // ── 4. Locate the OwnershipValidator ─────────────────────────────────────
        String resourceType = annotation.resourceType();
        OwnershipValidator validator = validators.get(resourceType);
        if (validator == null) {
            // Misconfiguration — fail loudly
            writeError(response, HttpStatus.INTERNAL_SERVER_ERROR,
                    "No ownership validator configured for resource type: " + resourceType);
            return;
        }

        // ── 5. Extract the resource ID ───────────────────────────────────────────
        String idParamName = annotation.idParamName();

        // The filter runs before the DispatcherServlet so path variables are not yet
        // bound as method arguments. We extract the ID directly from the request URI
        // using the path-variable template recorded on the handler mapping, or fall
        // back to query parameters.
        Long resourceId = extractFromPathVariables(request, idParamName);

        if (resourceId == null) {
            resourceId = extractFromQueryParam(request, idParamName);
        }

        if (resourceId == null) {
            resourceId = extractFromCachedBody(request, idParamName);
        }

        if (resourceId == null) {
            // Cannot enforce — let the request through; the application layer will
            // surface a proper validation error.
            filterChain.doFilter(request, response);
            return;
        }

        // ── 6. Validate ownership ────────────────────────────────────────────────
        try {
            validator.validateOwnership(resourceId, userId);
        } catch (ResourceNotFoundException ex) {
            writeError(response, HttpStatus.NOT_FOUND, ex.getMessage());
            return;
        } catch (ForbiddenException ex) {
            writeError(response, HttpStatus.FORBIDDEN, ex.getMessage());
            return;
        }

        filterChain.doFilter(request, response);
    }

    // ── Helper: resolve HandlerMethod ────────────────────────────────────────────

    private HandlerMethod resolveHandlerMethod(HttpServletRequest request) {
        try {
            HandlerExecutionChain chain = handlerMapping.getHandler(request);
            if (chain != null && chain.getHandler() instanceof HandlerMethod hm) {
                return hm;
            }
        } catch (Exception ignored) {
            // If resolution fails (e.g. no mapping) skip and let the chain handle it
        }
        return null;
    }

    // ── Helper: extract resource ID from path variables ───────────────────────────

    private Long extractFromPathVariables(HttpServletRequest request, String paramName) {
        // Spring stores already-decoded URI template variables in this request attribute
        // after DispatcherServlet processes them, but here we are still in the filter
        // chain before dispatch. We use the HandlerMapping attribute that the
        // RequestMappingHandlerMapping sets when getHandler() is called.
        Object vars = request.getAttribute(
                org.springframework.web.servlet.HandlerMapping.URI_TEMPLATE_VARIABLES_ATTRIBUTE);
        if (vars instanceof Map<?, ?> map) {
            Object value = map.get(paramName);
            if (value != null) {
                return convertToLong(value);
            }
        }
        return null;
    }

    // ── Helper: extract resource ID from query string ─────────────────────────────

    private Long extractFromQueryParam(HttpServletRequest request, String paramName) {
        String value = request.getParameter(paramName);
        return value != null ? convertToLong(value) : null;
    }

    // ── Helper: extract resource ID from a cached/readable request body ───────────

    private Long extractFromCachedBody(HttpServletRequest request, String idParamName) {
        // The raw body has already been consumed by the time many filters run.
        // We rely on Spring's ContentCachingRequestWrapper if present, otherwise skip.
        try {
            String body = null;
            if (request instanceof org.springframework.web.util.ContentCachingRequestWrapper wrapper) {
                byte[] buf = wrapper.getContentAsByteArray();
                if (buf.length > 0) {
                    body = new String(buf, wrapper.getCharacterEncoding());
                }
            }

            if (body == null || body.isBlank()) {
                return null;
            }

            // Parse as a generic map and look up the field
            @SuppressWarnings("unchecked")
            Map<String, Object> map = objectMapper.readValue(body, Map.class);
            Object value = map.get(idParamName);
            return value != null ? convertToLong(value) : null;
        } catch (Exception ignored) {
            return null;
        }
    }

    // ── Helper: type coercion ─────────────────────────────────────────────────────

    private Long convertToLong(Object arg) {
        return switch (arg) {
            case null -> null;
            case Long l -> l;
            case Integer i -> i.longValue();
            case Short s -> s.longValue();
            case String s -> {
                try { yield Long.parseLong(s); }
                catch (NumberFormatException e) { yield null; }
            }
            case Number n -> n.longValue();
            default -> null;
        };
    }

    // ── Helper: write a JSON error response ──────────────────────────────────────

    private void writeError(HttpServletResponse response, HttpStatus status, String message)
            throws IOException {
        response.setStatus(status.value());
        response.setContentType(MediaType.APPLICATION_JSON_VALUE);
        response.getWriter().write(
                objectMapper.writeValueAsString(new ResponseDTO<>("error", message, null)));
    }
}







