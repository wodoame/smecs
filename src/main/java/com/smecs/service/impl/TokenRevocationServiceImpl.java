package com.smecs.service.impl;

import com.smecs.config.CacheConfig;
import com.smecs.entity.RevokedToken;
import com.smecs.repository.RevokedTokenRepository;
import com.smecs.service.TokenRevocationService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.CachePut;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.time.Instant;
import java.util.HexFormat;

@Service
public class TokenRevocationServiceImpl implements TokenRevocationService {

    private final RevokedTokenRepository revokedTokenRepository;

    @Autowired
    public TokenRevocationServiceImpl(RevokedTokenRepository revokedTokenRepository) {
        this.revokedTokenRepository = revokedTokenRepository;
    }

    @Override
    @CachePut(
            value = CacheConfig.REVOKED_TOKENS,
            key = "T(com.smecs.service.impl.TokenRevocationServiceImpl).hashTokenForKey(#token)",
            condition = "#token != null && !#token.isBlank()"
    )
    public boolean revokeToken(String token, Instant expiresAt) {
        String tokenHash = hashToken(token);
        if (tokenHash == null) {
            return false;
        }

        if (!revokedTokenRepository.existsByTokenHash(tokenHash)) {
            RevokedToken revokedToken = new RevokedToken();
            revokedToken.setTokenHash(tokenHash);
            revokedToken.setExpiresAt(expiresAt);
            revokedTokenRepository.save(revokedToken);
        }

        revokedTokenRepository.deleteByExpiresAtBefore(Instant.now());
        return true;
    }

    @Override
    @Cacheable(
            value = CacheConfig.REVOKED_TOKENS,
            key = "T(com.smecs.service.impl.TokenRevocationServiceImpl).hashTokenForKey(#token)",
            condition = "#token != null && !#token.isBlank()",
            unless = "!#result"
    )
    public boolean isRevoked(String token) {
        String tokenHash = hashToken(token);
        if (tokenHash == null) {
            return false;
        }

        return revokedTokenRepository.existsByTokenHash(tokenHash);
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

    private String hashToken(String token) {
        return hashTokenForKey(token);
    }
}
