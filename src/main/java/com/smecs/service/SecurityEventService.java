package com.smecs.service;

import com.smecs.entity.User;
import jakarta.servlet.http.HttpServletRequest;

public interface SecurityEventService {
    void recordLoginSuccess(User user, HttpServletRequest request);

    void recordLoginFailure(String username, HttpServletRequest request);

    void recordTokenIssued(User user, String token, HttpServletRequest request);

    void recordTokenValidated(String token, String username, Long userId, HttpServletRequest request);

    void recordTokenRejected(String token, HttpServletRequest request);

    void recordOAuth2Success(User user, HttpServletRequest request);
}

