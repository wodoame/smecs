package com.smecs.service;

import com.smecs.dto.RequestMetadata;
import com.smecs.entity.User;

public interface SecurityEventService {
    void recordLoginSuccess(User user, RequestMetadata metadata);

    void recordLoginFailure(String username, RequestMetadata metadata);

    void recordTokenIssued(User user, String token, RequestMetadata metadata);

    void recordTokenValidated(String token, String username, Long userId, RequestMetadata metadata);

    void recordTokenRejected(String token, RequestMetadata metadata);

    void recordOAuth2Success(User user, RequestMetadata metadata);
}
