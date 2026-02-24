package com.smecs.config;

import com.smecs.interceptor.AuthInterceptor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class WebConfig implements WebMvcConfigurer {

    private final AuthInterceptor authInterceptor;

    @Autowired
    public WebConfig(AuthInterceptor authInterceptor) {
        this.authInterceptor = authInterceptor;
    }

    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        registry.addInterceptor(authInterceptor)
                .addPathPatterns("/api/**") // Apply to all API endpoints
                .excludePathPatterns("/api/auth/login", "/api/auth/register", "/api/products/**"); // Exclude
                                                                                                                         // public
                                                                                                                         // endpoints
                                                                                                                         // if
                                                                                                                         // needed,
                                                                                                                         // though
                                                                                                                         // annotation
                                                                                                                         // handles
                                                                                                                         // most
                                                                                                                         // logic
    }
}
