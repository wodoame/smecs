package com.smecs.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.smecs.dto.ResponseDTO;
import com.smecs.security.JwtAuthenticationFilter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

@Configuration
@EnableWebSecurity
@EnableMethodSecurity   // enables @PreAuthorize, @PostAuthorize, etc.
public class SecurityConfig {

    private final JwtAuthenticationFilter jwtAuthenticationFilter;

    @Autowired
    public SecurityConfig(JwtAuthenticationFilter jwtAuthenticationFilter) {
        this.jwtAuthenticationFilter = jwtAuthenticationFilter;
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    //noinspection RedundantThrows — throws Exception is required by the HttpSecurity builder API
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        // Local ObjectMapper — avoids a circular dependency with any app-level ObjectMapper bean
        ObjectMapper mapper = new ObjectMapper();

        http
            // Stateless JWT — no CSRF needed
            .csrf(AbstractHttpConfigurer::disable)

            // No HTTP session
            .sessionManagement(session ->
                    session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))

            // Authorization rules
            .authorizeHttpRequests(auth -> auth
                    // Public auth endpoints
                    .requestMatchers("/api/auth/login", "/api/auth/register").permitAll()
                    // Public read-only product, category, and review endpoints
                    .requestMatchers(HttpMethod.GET, "/api/products/**").permitAll()
                    .requestMatchers(HttpMethod.GET, "/api/categories/**").permitAll()
                    .requestMatchers(HttpMethod.GET, "/api/reviews/product/**").permitAll()
                    .requestMatchers(HttpMethod.GET, "/api/reviews/{reviewId}").permitAll()
                    // GraphQL endpoint — method-level @PreAuthorize handles it
                    .requestMatchers("/graphql", "/graphiql/**").permitAll()
                    // SpringDoc / Swagger UI
                    .requestMatchers("/v3/api-docs/**", "/swagger-ui/**", "/swagger-ui.html").permitAll()
                    // Static assets and Thymeleaf views
                    .requestMatchers("/", "/index.html", "/static/**", "/assets/**", "/*.svg").permitAll()
                    // Everything else requires a valid token
                    .anyRequest().authenticated()
            )

            // Return JSON error bodies (matching our ResponseDTO shape) instead of HTML pages
            .exceptionHandling(ex -> ex
                    .authenticationEntryPoint((_req, response, _cause) -> {
                        response.setStatus(HttpStatus.UNAUTHORIZED.value());
                        response.setContentType(MediaType.APPLICATION_JSON_VALUE);
                        response.getWriter().write(
                                mapper.writeValueAsString(
                                        new ResponseDTO<>("error", "Authentication required", null)));
                    })
                    .accessDeniedHandler((_req, response, _cause) -> {
                        response.setStatus(HttpStatus.FORBIDDEN.value());
                        response.setContentType(MediaType.APPLICATION_JSON_VALUE);
                        response.getWriter().write(
                                mapper.writeValueAsString(
                                        new ResponseDTO<>("error", "Insufficient permissions", null)));
                    })
            )

            // Validate JWT before the standard username/password filter
            .addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }
}
