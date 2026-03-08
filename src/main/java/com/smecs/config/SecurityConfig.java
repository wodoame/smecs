package com.smecs.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.smecs.dto.ResponseDTO;
import com.smecs.security.JwtAuthenticationFilter;
import com.smecs.security.OAuth2AuthenticationSuccessHandler;
import lombok.AllArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.security.oauth2.client.registration.ClientRegistrationRepository;
import org.springframework.security.oauth2.client.web.DefaultOAuth2AuthorizationRequestResolver;
import org.springframework.security.oauth2.client.web.OAuth2AuthorizationRequestResolver;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import java.util.List;

@AllArgsConstructor(onConstructor_ = @Autowired)
@Configuration
@EnableWebSecurity
@EnableMethodSecurity   // enables @PreAuthorize, @PostAuthorize, etc.
public class SecurityConfig {

    private final JwtAuthenticationFilter jwtAuthenticationFilter;
    private final OAuth2AuthenticationSuccessHandler oAuth2SuccessHandler;
    private final ClientRegistrationRepository clientRegistrationRepository;


    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        ObjectMapper mapper = new ObjectMapper();

        http
            .cors(cors -> cors.configurationSource(corsConfigurationSource()))
            // Keep CSRF for browser form flows, but ignore stateless JWT/API endpoints.
            .csrf(csrf -> csrf.ignoringRequestMatchers(
                    "/api/**",
                    "/graphql",
                    "/graphiql/**",
                    "/oauth2/**",
                    "/login/oauth2/**"
            ))

            .sessionManagement(session ->
                    session.sessionCreationPolicy(SessionCreationPolicy.IF_REQUIRED))

            // Authorization rules
            .authorizeHttpRequests(auth -> auth
                    // Public auth endpoints
                    .requestMatchers("/api/auth/login", "/api/auth/register").permitAll()
                    // OAuth2 authorization + callback endpoints
                    .requestMatchers("/oauth2/**", "/login/oauth2/**").permitAll()
                    // Public read-only product, category, and review endpoints
                    .requestMatchers(HttpMethod.GET, "/api/products/**").permitAll()
                    .requestMatchers(HttpMethod.GET, "/api/categories/**").permitAll()
                    .requestMatchers(HttpMethod.GET, "/api/reviews/product/**").permitAll()
                    .requestMatchers(HttpMethod.GET, "/api/reviews/{reviewId}").permitAll()
                    // GraphQL endpoint — method-level @PreAuthorize handles it
                    .requestMatchers("/graphql", "/graphiql/**").permitAll()
                    // SpringDoc / Swagger UI
                    .requestMatchers("/v3/api-docs/**", "/swagger-ui/**", "/swagger-ui.html").permitAll()
                    // CSRF demo form endpoint (intentionally browser/session based)
                    .requestMatchers("/security/csrf-demo").permitAll()
                    // Static assets and Thymeleaf views
                    .requestMatchers("/", "/index.html", "/static/**", "/assets/**", "/*.svg").permitAll()
                    .requestMatchers("/products", "/products/{id}", "/categories", "/categories/{id}", "/login", "/signup").permitAll()
                    // Everything else requires a valid token
                    .anyRequest().authenticated()
            )

            // Google OAuth2 login
            .oauth2Login(oauth2 -> oauth2
                    .authorizationEndpoint(endpoint ->
                            endpoint.baseUri("/oauth2/authorization")
                                    .authorizationRequestResolver(
                                            authorizationRequestResolver(clientRegistrationRepository)))
                    .redirectionEndpoint(endpoint ->
                            endpoint.baseUri("/login/oauth2/code/*"))
                    .successHandler(oAuth2SuccessHandler)
                    .failureHandler((_req, response, exception) -> {
                        response.setStatus(HttpStatus.UNAUTHORIZED.value());
                        response.setContentType(MediaType.APPLICATION_JSON_VALUE);
                        response.getWriter().write(
                                mapper.writeValueAsString(
                                        new ResponseDTO<>("error",
                                                "Google login failed: " + exception.getMessage(), null)));
                    })
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
            .addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class)
            ;

        return http.build();
    }

    private OAuth2AuthorizationRequestResolver authorizationRequestResolver(
            ClientRegistrationRepository clientRegistrationRepository) {
        DefaultOAuth2AuthorizationRequestResolver resolver =
                new DefaultOAuth2AuthorizationRequestResolver(clientRegistrationRepository, "/oauth2/authorization");
        resolver.setAuthorizationRequestCustomizer(customizer ->
                customizer.additionalParameters(params -> params.put("prompt", "select_account")));
        return resolver;
    }

    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration config = new CorsConfiguration();
        config.setAllowedOrigins(List.of("http://localhost:8080"));
        config.setAllowedMethods(List.of("GET", "POST", "PUT", "DELETE", "OPTIONS"));
        config.setAllowedHeaders(List.of("Authorization", "Content-Type"));
        config.setAllowCredentials(true);

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", config);
        return source;
    }
}
