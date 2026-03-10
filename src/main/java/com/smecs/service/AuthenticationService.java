package com.smecs.service;

import org.springframework.security.core.Authentication;

public interface AuthenticationService {
    Authentication authenticateUser(String usernameOrEmail, String password);
}
