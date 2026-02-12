package com.smecs.interceptor;

import com.smecs.annotation.RequireRole;
import com.smecs.util.JwtUtil;
import com.smecs.util.RoleHierarchy;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.jspecify.annotations.NonNull;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.method.HandlerMethod;
import org.springframework.web.servlet.HandlerInterceptor;


@Component
public class AuthInterceptor implements HandlerInterceptor {

    private final JwtUtil jwtUtil;

    @Autowired
    public AuthInterceptor(JwtUtil jwtUtil) {
        this.jwtUtil = jwtUtil;
    }

    @Override
    public boolean preHandle(@NonNull HttpServletRequest request, @NonNull HttpServletResponse response,
            @NonNull Object handler)
            throws Exception {
        if (!(handler instanceof HandlerMethod handlerMethod)) {
            return true;
        }

        RequireRole requireRole = handlerMethod.getMethodAnnotation(RequireRole.class);
        if (requireRole == null) {
            requireRole = handlerMethod.getBeanType().getAnnotation(RequireRole.class);
        }

        if (requireRole == null) {
            return true; // Public endpoint
        }

        String authHeader = request.getHeader("Authorization");
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            response.sendError(HttpServletResponse.SC_UNAUTHORIZED, "Missing or invalid Authorization header");
            return false;
        }

        String token = authHeader.substring(7);
        if (!jwtUtil.validateToken(token)) {
            response.sendError(HttpServletResponse.SC_UNAUTHORIZED, "Invalid token");
            return false;
        }

        String userRole = jwtUtil.extractRole(token);
        String[] requiredRoles = requireRole.value();

        if (requiredRoles.length > 0) {
            boolean hasPermission = RoleHierarchy.hasPermission(userRole, requiredRoles);
            if (!hasPermission) {
                response.sendError(HttpServletResponse.SC_FORBIDDEN, "Insufficient permissions");
                return false;
            }
        }

        // Add user info to request attributes for use in controllers if needed
        request.setAttribute("userId", jwtUtil.extractUserId(token));
        request.setAttribute("username", jwtUtil.extractUsername(token));
        request.setAttribute("role", userRole);

        return true;
    }
}
