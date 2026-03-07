package com.smecs.service.impl;

import com.smecs.entity.User;
import com.smecs.repository.UserRepository;
import com.smecs.service.AuthenticationService;
import lombok.AllArgsConstructor;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.AuthenticationException;
import org.springframework.stereotype.Service;

@AllArgsConstructor
@Service
public class AuthenticationServiceImpl implements AuthenticationService {

    private final UserRepository userRepository;
    private final ObjectProvider<AuthenticationManager> authenticationManagerProvider;

    @Override
    public User authenticateUser(String usernameOrEmail, String password) {
        String principal = usernameOrEmail.trim();
        try {
            AuthenticationManager authenticationManager = authenticationManagerProvider.getObject();
            authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(principal, password));
            return findByUsernameOrEmail(principal);
        } catch (AuthenticationException ex) {
            return null;
        }
    }

    private User findByUsernameOrEmail(String usernameOrEmail) {
        User user = userRepository.findByUsername(usernameOrEmail).orElse(null);
        if (user != null) {
            return user;
        }
        return userRepository.findByEmail(usernameOrEmail).orElse(null);
    }
}

