package com.smecs.aspect;

/**
 * Removed — replaced by Spring Security's JwtAuthenticationFilter + @PreAuthorize.
 *
 * Previously used AOP to validate JWT tokens and enforce @RequireRole for GraphQL resolvers.
 * That responsibility is now handled by:
 *   - JwtAuthenticationFilter: validates the token and populates the SecurityContext
 *   - @PreAuthorize on GraphQLController methods: enforces role requirements
 *   - SecurityConfig: configures the filter chain and access rules
 *
 * @deprecated This class is no longer active. It is retained as an empty placeholder.
 */
@SuppressWarnings("unused")
class GraphQLSecurityAspect {
    // Intentionally empty — replaced by Spring Security
}
