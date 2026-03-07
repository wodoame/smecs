package com.smecs.service;

import com.smecs.entity.User;

public interface AuthenticationService {
    User authenticateUser(String usernameOrEmail, String password);
}

