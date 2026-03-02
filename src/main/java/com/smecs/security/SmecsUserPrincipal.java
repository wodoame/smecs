package com.smecs.security;

import lombok.Getter;

import java.io.Serial;
import java.io.Serializable;

/**
 * Custom principal stored in the Spring SecurityContext.
 * Carries userId, cartId, and role so that controllers and aspects can
 * read them from the SecurityContext instead of raw request attributes.
 */
@Getter
public class SmecsUserPrincipal implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    private final Long userId;
    private final String username;
    private final String role;
    private final Long cartId;

    public SmecsUserPrincipal(Long userId, String username, String role, Long cartId) {
        this.userId = userId;
        this.username = username;
        this.role = role;
        this.cartId = cartId;
    }

    @Override
    public String toString() {
        return "SmecsUserPrincipal{userId=" + userId + ", username='" + username + "', role='" + role + "'}";
    }
}


