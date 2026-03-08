package com.smecs.service.impl;

import com.smecs.entity.User;
import com.smecs.security.SmecsUserPrincipal;
import com.smecs.service.AuthenticationService;
import lombok.AllArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.AuthenticationException;
import org.springframework.stereotype.Service;

@AllArgsConstructor
@Service
public class AuthenticationServiceImpl implements AuthenticationService {

    private final AuthenticationManager authenticationManager;
    @Override
    public User authenticateUser(String usernameOrEmail, String password) {
        String principal = usernameOrEmail.trim();
        try {
            Authentication authentication = authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(principal, password));
            Object authenticatedPrincipal = authentication.getPrincipal();
            if (authenticatedPrincipal instanceof SmecsUserPrincipal smecsPrincipal) {
                return toUser(smecsPrincipal);
            }
            return null;
        } catch (AuthenticationException ex) {
            return null;
        }
    }

    private User toUser(SmecsUserPrincipal principal) {
        User user = new User();
        user.setId(principal.getUserId());
        user.setUsername(principal.getUsername());
        user.setEmail(principal.getEmail());
        user.setRole(principal.getRole());
        return user;
    }
}
