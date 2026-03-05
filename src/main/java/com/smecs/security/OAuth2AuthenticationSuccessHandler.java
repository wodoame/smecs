package com.smecs.security;

import com.smecs.entity.Cart;
import com.smecs.entity.User;
import com.smecs.service.CartService;
import com.smecs.service.SecurityEventService;
import com.smecs.service.UserService;
import com.smecs.util.JwtUtil;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.jspecify.annotations.NonNull;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.security.web.authentication.SimpleUrlAuthenticationSuccessHandler;
import org.springframework.stereotype.Component;
import org.springframework.web.util.UriComponentsBuilder;

import java.io.IOException;
import java.util.Arrays;

/**
 * Invoked by Spring Security after a successful Google OAuth2 login.

 * Responsibilities:
 *   1. Resolve (find or create) the local User record.
 *   2. Ensure the user has a Cart.
 *   3. Mint a JWT using the existing JwtUtil.
 *   4. Redirect the React SPA to /oauth2/callback?token=…&cartId=…
 *      (plus an optional ?next= param read from the pre-redirect cookie).
 */
@Component
public class OAuth2AuthenticationSuccessHandler extends SimpleUrlAuthenticationSuccessHandler {

    static final String NEXT_COOKIE = "oauth2_next";
    static final String CALLBACK_PATH = "/oauth2/callback";

    private final UserService userService;
    private final CartService cartService;
    private final JwtUtil jwtUtil;
    private final SecurityEventService securityEventService;

    @Autowired
    public OAuth2AuthenticationSuccessHandler(UserService userService,
                                              CartService cartService,
                                              JwtUtil jwtUtil,
                                              SecurityEventService securityEventService) {
        this.userService = userService;
        this.cartService = cartService;
        this.jwtUtil = jwtUtil;
        this.securityEventService = securityEventService;
    }

    @Override
    public void onAuthenticationSuccess(@NonNull HttpServletRequest request,
                                        @NonNull HttpServletResponse response,
                                        Authentication authentication) throws IOException {

        OAuth2User oAuth2User = (OAuth2User) authentication.getPrincipal();

        // 1. Find or create the local User record
        User user = userService.findOrCreateOAuthUser(oAuth2User, "google");

        // 2. Ensure the user has a Cart (createCart is idempotent)
        Cart cart = cartService.createCart(user.getId());

        // 3. Mint a JWT the same way the password-login endpoint does
        String token = jwtUtil.generateToken(user, cart.getCartId());
        securityEventService.recordOAuth2Success(user, request);
        securityEventService.recordTokenIssued(user, token, request);

        // 4. Build the redirect URL
        String next = extractNextFromCookie(request);
        clearNextCookie(response);

        String redirectUrl = UriComponentsBuilder
                .fromPath(CALLBACK_PATH)
                .queryParam("token", token)
                .queryParam("cartId", cart.getCartId())
                .queryParam("next", next != null ? next : "/")
                .build()
                .toUriString();

        getRedirectStrategy().sendRedirect(request, response, redirectUrl);
    }

    // ── helpers ─────────────────────────────────────────────────────────────

    private String extractNextFromCookie(HttpServletRequest request) {
        if (request.getCookies() == null) return null;
        return Arrays.stream(request.getCookies())
                .filter(c -> NEXT_COOKIE.equals(c.getName()))
                .map(Cookie::getValue)
                .findFirst()
                .orElse(null);
    }

    private void clearNextCookie(HttpServletResponse response) {
        Cookie cookie = new Cookie(NEXT_COOKIE, "");
        cookie.setMaxAge(0);
        cookie.setPath("/");
        response.addCookie(cookie);
    }
}
