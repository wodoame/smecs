package com.smecs.security;

import jakarta.servlet.http.HttpServletResponse;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseCookie;

public final class AuthCookieUtils {

    public static final String ACCESS_TOKEN_COOKIE = "smecs_access_token";

    private AuthCookieUtils() {
    }

    public static void addAccessTokenCookie(HttpServletResponse response, String token, long maxAgeSeconds) {
        response.addHeader(HttpHeaders.SET_COOKIE, ResponseCookie.from(ACCESS_TOKEN_COOKIE, token)
                .httpOnly(true)
                .secure(false)
                .sameSite("Lax")
                .path("/")
                .maxAge(maxAgeSeconds)
                .build()
                .toString());
    }

    public static void clearAccessTokenCookie(HttpServletResponse response) {
        response.addHeader(HttpHeaders.SET_COOKIE, ResponseCookie.from(ACCESS_TOKEN_COOKIE, "")
                .httpOnly(true)
                .secure(false)
                .sameSite("Lax")
                .path("/")
                .maxAge(0)
                .build()
                .toString());
    }
}
