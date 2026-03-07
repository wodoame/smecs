package com.smecs.security;

import lombok.Getter;

import java.io.Serial;
import java.io.Serializable;

/**
 * Custom principal stored in the Spring SecurityContext.
 * Carries userId and role so that controllers and aspects can
 * read them from the SecurityContext instead of raw request attributes.
 */
@Getter
public class SmecsUserPrincipal implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    private final Long userId;
    private final String username;
    private final String email;
    private final String role;

    public SmecsUserPrincipal(Long userId, String username, String email, String role) {
        this.userId = userId;
        this.username = username;
        this.email = email;
        this.role = role;
    }

    @Override
    public String toString() {
        return "SmecsUserPrincipal{userId=" + userId + ", username='" + username + "', role='" + role + "'}";
    }
}
