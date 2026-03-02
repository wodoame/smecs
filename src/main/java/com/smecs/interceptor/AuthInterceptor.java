package com.smecs.interceptor;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.jspecify.annotations.NonNull;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

/**
 * Removed — authentication is now handled by Spring Security's JwtAuthenticationFilter.
 * This interceptor is retained as a no-op so that WebConfig continues to compile.
 *
 * @deprecated No longer performs any security checks.
 */
@Component
public class AuthInterceptor implements HandlerInterceptor {

    @Override
    public boolean preHandle(@NonNull HttpServletRequest request,
                             @NonNull HttpServletResponse response,
                             @NonNull Object handler) {
        // No-op: Spring Security's JwtAuthenticationFilter handles authentication
        return true;
    }
}
