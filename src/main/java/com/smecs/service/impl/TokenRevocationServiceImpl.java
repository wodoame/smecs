package com.smecs.service.impl;

import com.smecs.service.TokenRevocationService;
import org.springframework.stereotype.Service;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.time.Instant;
import java.util.HexFormat;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Service
public class TokenRevocationServiceImpl implements TokenRevocationService {

    private final Map<String, Instant> revokedTokenExpirations = new ConcurrentHashMap<>();

    @Override
    public boolean revokeToken(String token, Instant expiresAt) {
        String tokenHash = hashTokenForKey(token);
        if (tokenHash == null || expiresAt == null) {
            return false;
        }

        revokedTokenExpirations.put(tokenHash, expiresAt);
        cleanupExpired(Instant.now());
        return true;
    }

    @Override
    public boolean isRevoked(String token) {
        String tokenHash = hashTokenForKey(token);
        if (tokenHash == null) {
            return false;
        }

        Instant now = Instant.now();
        Instant expiresAt = revokedTokenExpirations.get(tokenHash);
        if (expiresAt == null) {
            return false;
        }
        if (!expiresAt.isAfter(now)) {
            revokedTokenExpirations.remove(tokenHash);
            return false;
        }
        return true;
    }

    public static String hashTokenForKey(String token) {
        if (token == null || token.isBlank()) {
            return null;
        }
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hashed = digest.digest(token.getBytes(StandardCharsets.UTF_8));
            return HexFormat.of().formatHex(hashed);
        } catch (Exception e) {
            return null;
        }
    }

    private void cleanupExpired(Instant now) {
        revokedTokenExpirations.entrySet().removeIf(entry -> !entry.getValue().isAfter(now));
    }
}
