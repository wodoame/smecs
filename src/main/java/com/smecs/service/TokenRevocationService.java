package com.smecs.service;

import java.time.Instant;

public interface TokenRevocationService {
    boolean revokeToken(String token, Instant expiresAt);

    boolean isRevoked(String token);
}
