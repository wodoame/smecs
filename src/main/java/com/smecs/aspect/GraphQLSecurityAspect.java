package com.smecs.aspect;

import com.smecs.annotation.RequireRole;
import com.smecs.exception.ForbiddenException;
import com.smecs.exception.UnauthorizedException;
import com.smecs.util.JwtUtil;
import com.smecs.util.RoleHierarchy;
import jakarta.servlet.http.HttpServletRequest;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Before;
import org.aspectj.lang.reflect.MethodSignature;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import java.lang.reflect.Method;

@Aspect
@Component
@Order(1) // Run first, before OwnershipAspect
public class GraphQLSecurityAspect {

    private final JwtUtil jwtUtil;

    @Autowired
    public GraphQLSecurityAspect(JwtUtil jwtUtil) {
        this.jwtUtil = jwtUtil;
    }

    /**
     * Intercepts methods or classes annotated with @RequireRole.
     * Use AOP to check security for GraphQL since HandlerInterceptors don't wrap
     * resolver methods.
     */
    @Before("@annotation(com.smecs.annotation.RequireRole) || @within(com.smecs.annotation.RequireRole)")
    public void checkSecurity(JoinPoint joinPoint) {
        ServletRequestAttributes attributes = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
        if (attributes == null) {
            // Not a web request (e.g. background job), skip security check or fail
            // depending on policy.
            // Here we skip, assuming non-web invocations are internal/trusted.
            return;
        }

        HttpServletRequest request = attributes.getRequest();

        // Check for annotation on method, then class
        MethodSignature signature = (MethodSignature) joinPoint.getSignature();
        Method method = signature.getMethod();
        RequireRole requireRole = method.getAnnotation(RequireRole.class);
        if (requireRole == null) {
            requireRole = joinPoint.getTarget().getClass().getAnnotation(RequireRole.class);
        }

        if (requireRole == null) {
            return; // Should not happen due to pointcut
        }

        // Get Authorization header
        String authHeader = request.getHeader("Authorization");
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            throw new UnauthorizedException("Missing or invalid Authorization header");
        }

        String token = authHeader.substring(7);

        // Validate Token
        if (!jwtUtil.validateToken(token)) {
            throw new UnauthorizedException("Invalid or expired token");
        }

        // Check Permissions
        String userRole = jwtUtil.extractRole(token);
        String[] requiredRoles = requireRole.value();

        if (requiredRoles.length > 0) {
            boolean hasPermission = RoleHierarchy.hasPermission(userRole, requiredRoles);
            if (!hasPermission) {
                throw new ForbiddenException(
                        "Insufficient permissions: requires one of " + String.join(", ", requiredRoles));
            }
        }

        // Populate request attributes so resolvers can access user info
        if (request.getAttribute("userId") == null) {
            request.setAttribute("userId", jwtUtil.extractUserId(token));
            request.setAttribute("username", jwtUtil.extractUsername(token));
            request.setAttribute("role", userRole);
        }
    }
}
