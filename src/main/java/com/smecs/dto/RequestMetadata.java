package com.smecs.dto;

import jakarta.servlet.http.HttpServletRequest;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Capture metadata from HttpServletRequest to pass safely to async methods.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RequestMetadata {
    private String ipAddress;
    private String userAgent;
    private String endpoint;

    public static RequestMetadata from(HttpServletRequest request) {
        if (request == null) return new RequestMetadata();
        
        return RequestMetadata.builder()
                .ipAddress(extractClientIp(request))
                .userAgent(request.getHeader("User-Agent"))
                .endpoint(request.getRequestURI())
                .build();
    }

    private static String extractClientIp(HttpServletRequest request) {
        String forwarded = request.getHeader("X-Forwarded-For");
        if (forwarded != null && !forwarded.isBlank()) {
            return forwarded.split(",")[0].trim();
        }
        return request.getRemoteAddr();
    }
}
