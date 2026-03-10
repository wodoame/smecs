package com.smecs.service.impl;

import com.smecs.service.AuthenticationService;
import lombok.AllArgsConstructor;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;

@AllArgsConstructor
@Service
public class AuthenticationServiceImpl implements AuthenticationService {

    private final AuthenticationManager authenticationManager;

    @Override
    public Authentication authenticateUser(String usernameOrEmail, String password) {
        return authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(usernameOrEmail.trim(), password));
    }
}
