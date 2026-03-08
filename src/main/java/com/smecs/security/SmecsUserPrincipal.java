package com.smecs.security;

import lombok.Getter;
import org.jspecify.annotations.NullMarked;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.io.Serial;
import java.io.Serializable;
import java.util.Collection;
import java.util.List;

/**
 * Custom principal stored in the Spring SecurityContext.
 * Carries userId and role so that controllers and aspects can
 * read them from the SecurityContext instead of raw request attributes.
 */
@Getter
public class SmecsUserPrincipal implements UserDetails, Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    private final Long userId;
    private final String username;
    private final String email;
    private final String role;
    private final String password;
    private final List<SimpleGrantedAuthority> authorities;

    public SmecsUserPrincipal(Long userId, String username, String email, String role) {
        this(userId, username, email, role, null);
    }

    public SmecsUserPrincipal(Long userId, String username, String email, String role, String password) {
        this.userId = userId;
        this.username = username;
        this.email = email;
        this.role = role;
        this.password = password;
        String effectiveRole = (role != null && !role.isBlank()) ? role : "customer";
        this.authorities = List.of(new SimpleGrantedAuthority("ROLE_" + effectiveRole.toUpperCase()));
    }

    @Override
    public String toString() {
        return "SmecsUserPrincipal{userId=" + userId + ", username='" + username + "', role='" + role + "'}";
    }

    @Override
    @NullMarked
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return authorities;
    }

    @Override
    public String getPassword() {
        return password;
    }
}
