package com.smecs.aspect;

import com.smecs.annotation.RequireOwnership;
import com.smecs.exception.ForbiddenException;
import com.smecs.exception.UnauthorizedException;
import com.smecs.validation.OwnershipValidator;
import jakarta.servlet.http.HttpServletRequest;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Before;
import org.aspectj.lang.reflect.MethodSignature;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import java.lang.annotation.Annotation;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.Parameter;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * Aspect that enforces resource ownership checks for methods annotated with @RequireOwnership.
 * This aspect runs after GraphQLSecurityAspect (which validates JWT and sets user attributes).
 * Admins automatically bypass ownership checks.
 */
@Aspect
@Component
@Order(2) // Run after GraphQLSecurityAspect (which has default order)
public class OwnershipAspect {

    private final Map<String, OwnershipValidator> validators;

    @Autowired
    public OwnershipAspect(List<OwnershipValidator> validatorList) {
        // Create a map of resourceType -> validator for quick lookup
        this.validators = validatorList.stream()
                .collect(Collectors.toMap(
                        OwnershipValidator::getResourceType,
                        Function.identity()
                ));
    }

    /**
     * Intercepts methods annotated with @RequireOwnership and validates ownership.
     */
    @Before("@annotation(com.smecs.annotation.RequireOwnership)")
    public void checkOwnership(JoinPoint joinPoint) {
        ServletRequestAttributes attributes = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
        if (attributes == null) {
            // Not a web request, skip ownership check
            return;
        }

        HttpServletRequest request = attributes.getRequest();

        // Extract user information from request attributes (set by GraphQLSecurityAspect)
        Long userId = (Long) request.getAttribute("userId");
        String role = (String) request.getAttribute("role");

        if (userId == null) {
            throw new UnauthorizedException("User authentication required for ownership validation");
        }

        // Admins bypass ownership checks
        if ("admin".equalsIgnoreCase(role)) {
            return;
        }

        // Get the annotation
        MethodSignature signature = (MethodSignature) joinPoint.getSignature();
        Method method = signature.getMethod();
        RequireOwnership requireOwnership = method.getAnnotation(RequireOwnership.class);

        if (requireOwnership == null) {
            return; // Should not happen due to pointcut
        }

        String resourceType = requireOwnership.resourceType();
        String idParamName = requireOwnership.idParamName();
        String requestBodyParam = requireOwnership.requestBodyParam();

        // Get the validator for this resource type
        OwnershipValidator validator = validators.get(resourceType);
        if (validator == null) {
            throw new IllegalStateException("No ownership validator found for resource type: " + resourceType);
        }

        // 1) Try original behavior: parameter name match (requires -parameters)
        Long resourceId = extractResourceIdFromParameters(method, joinPoint.getArgs(), idParamName);

        // 2) Try matching @PathVariable or @RequestParam annotation values
        if (resourceId == null) {
            resourceId = extractResourceIdFromAnnotatedParams(method, joinPoint.getArgs(), idParamName);
        }

        // 3) Fallback: inspect @RequestBody parameter (auto-detect or by requestBodyParam)
        if (resourceId == null) {
            Object body = findRequestBodyArgument(method, joinPoint.getArgs(), requestBodyParam);
            if (body != null) {
                resourceId = extractFromObject(body, idParamName);
            }
        }

        if (resourceId == null) {
            throw new IllegalArgumentException("Could not find parameter or request body property '" + idParamName + "'");
        }

        // Validate ownership - validator will throw ResourceNotFoundException or ForbiddenException
        validator.validateOwnership(resourceId, userId);
    }

    private Long extractResourceIdFromParameters(Method method, Object[] args, String paramName) {
        Parameter[] parameters = method.getParameters();
        for (int i = 0; i < parameters.length; i++) {
            Parameter parameter = parameters[i];
            if (parameter.getName().equals(paramName)) {
                Object arg = args[i];
                Long val = convertToLong(arg);
                if (val != null) return val;
            }
        }
        return null;
    }

    private Long extractResourceIdFromAnnotatedParams(Method method, Object[] args, String idParamName) {
        Parameter[] parameters = method.getParameters();
        for (int i = 0; i < parameters.length; i++) {
            Parameter parameter = parameters[i];
            for (Annotation annotation : parameter.getAnnotations()) {
                if (annotation instanceof PathVariable pv) {
                    String name = pv.value();
                    if (name.isEmpty()) name = parameter.getName();
                    if (name.equals(idParamName)) {
                        return convertToLong(args[i]);
                    }
                } else if (annotation instanceof RequestParam rp) {
                    String name = rp.value();
                    if (name.isEmpty()) name = parameter.getName();
                    if (name.equals(idParamName)) {
                        return convertToLong(args[i]);
                    }
                }
            }
        }
        return null;
    }

    private Object findRequestBodyArgument(Method method, Object[] args, String requestBodyParam) {
        Parameter[] parameters = method.getParameters();
        // If caller specified a requestBodyParam, try to match by parameter simple type name or by parameter name
        if (requestBodyParam != null && !requestBodyParam.isEmpty()) {
            for (int i = 0; i < parameters.length; i++) {
                Parameter parameter = parameters[i];
                // match by simple class name
                if (parameter.getType().getSimpleName().equals(requestBodyParam)) {
                    return args[i];
                }
                // match by parameter name if available
                if (parameter.getName().equals(requestBodyParam)) {
                    return args[i];
                }
            }
        }

        // Otherwise auto-detect first parameter annotated with @RequestBody
        for (int i = 0; i < parameters.length; i++) {
            if (parameters[i].isAnnotationPresent(RequestBody.class)) {
                return args[i];
            }
        }
        return null;
    }

    private Long extractFromObject(Object obj, String fieldName) {
        if (obj == null) return null;
        Class<?> cls = obj.getClass();
        String capitalized = Character.toUpperCase(fieldName.charAt(0)) + fieldName.substring(1);
        String[] getterNames = new String[]{"get" + capitalized, "is" + capitalized, fieldName};

        // Try getters
        for (String getterName : getterNames) {
            try {
                Method m = cls.getMethod(getterName);
                Object value = m.invoke(obj);
                Long converted = convertToLong(value);
                if (converted != null) return converted;
            } catch (NoSuchMethodException ignored) {
                // try next
            } catch (ReflectiveOperationException | IllegalArgumentException e) {
                // ignore and continue
            }
        }

        // Try direct field access
        try {
            Field f = cls.getDeclaredField(fieldName);
            f.setAccessible(true);
            Object value = f.get(obj);
            return convertToLong(value);
        } catch (NoSuchFieldException ignored) {
            // ignore
        } catch (IllegalAccessException | IllegalArgumentException e) {
            // ignore
        }

        return null;
    }

    private Long convertToLong(Object arg) {
        if (arg == null) return null;
        if (arg instanceof Long) return (Long) arg;
        if (arg instanceof Integer) return ((Integer) arg).longValue();
        if (arg instanceof Short) return ((Short) arg).longValue();
        if (arg instanceof String) {
            try {
                return Long.parseLong((String) arg);
            } catch (NumberFormatException e) {
                return null;
            }
        }
        if (arg instanceof Number) {
            return ((Number) arg).longValue();
        }
        return null;
    }
}
