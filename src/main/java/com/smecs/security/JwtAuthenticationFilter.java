package com.smecs.security;

import com.smecs.service.SecurityEventService;
import com.smecs.util.JwtUtil;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.jspecify.annotations.NonNull;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.List;

/**
 * JWT authentication filter that validates the Bearer token on every request
 * and populates the Spring SecurityContext so that @PreAuthorize and other
 * Spring Security mechanisms can work correctly.
 */
@Component
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private final JwtUtil jwtUtil;
    private final SecurityEventService securityEventService;

    @Autowired
    public JwtAuthenticationFilter(JwtUtil jwtUtil, SecurityEventService securityEventService) {
        this.jwtUtil = jwtUtil;
        this.securityEventService = securityEventService;
    }

    @Override
    protected void doFilterInternal(@NonNull HttpServletRequest request,
                                    @NonNull HttpServletResponse response,
                                    @NonNull FilterChain filterChain)
            throws ServletException, IOException {

        String authHeader = request.getHeader("Authorization");

        if (authHeader != null && authHeader.startsWith("Bearer ")) {
            String token = authHeader.substring(7);

            if (jwtUtil.validateToken(token)) {
                String username = jwtUtil.extractUsername(token);
                String role     = jwtUtil.extractRole(token);
                String email    = jwtUtil.extractEmail(token);
                Long userId     = jwtUtil.extractUserId(token);

                // Spring Security roles must be prefixed with ROLE_.
                // Fall back to CUSTOMER if the claim is absent (e.g. legacy tokens).
                String effectiveRole = (role != null && !role.isBlank()) ? role : "customer";
                String springRole = "ROLE_" + effectiveRole.toUpperCase();
                List<SimpleGrantedAuthority> authorities = List.of(new SimpleGrantedAuthority(springRole));

                SmecsUserPrincipal principal = new SmecsUserPrincipal(userId, username, email, role);

                UsernamePasswordAuthenticationToken authentication =
                        new UsernamePasswordAuthenticationToken(principal, null, authorities);
                authentication.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));

                SecurityContextHolder.getContext().setAuthentication(authentication);
                securityEventService.recordTokenValidated(token, username, userId, request);
            } else {
                securityEventService.recordTokenRejected(token, request);
            }
        }

        filterChain.doFilter(request, response);
    }
}
