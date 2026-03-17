package com.smecs.service.impl;

import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.Caffeine;
import com.smecs.dto.RequestMetadata;
import com.smecs.entity.SecurityEvent;
import com.smecs.entity.SecurityEventType;
import com.smecs.entity.User;
import com.smecs.repository.SecurityEventRepository;
import com.smecs.service.SecurityEventService;
import org.springframework.beans.factory.annotation.Autowired;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.time.Duration;
import java.util.HexFormat;
import java.util.Objects;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;

@Service
public class SecurityEventServiceImpl implements SecurityEventService {

    private static final Logger log = LoggerFactory.getLogger(SecurityEventServiceImpl.class);

    private static final int BRUTE_FORCE_THRESHOLD = 5;
    private static final Duration BRUTE_FORCE_WINDOW = Duration.ofMinutes(10);

    private final SecurityEventRepository securityEventRepository;
    private final Cache<String, FailedLoginTracker> failedLoginCache;

    @Autowired
    public SecurityEventServiceImpl(SecurityEventRepository securityEventRepository) {
        this.securityEventRepository = securityEventRepository;
        this.failedLoginCache = Caffeine.newBuilder()
                .expireAfterWrite(BRUTE_FORCE_WINDOW)
                .maximumSize(10_000)
                .build();
    }

    @Async
    @Override
    public void recordLoginSuccess(User user, RequestMetadata metadata) {
        log.info("recordLoginSuccess invoked on thread={}", Thread.currentThread().getName());
        SecurityEvent event = baseEvent(SecurityEventType.LOGIN_SUCCESS, metadata);
        event.setUserId(user.getId());
        event.setUsername(user.getUsername());
        securityEventRepository.save(event);
    }

    @Async
    @Override
    public void recordLoginFailure(String username, RequestMetadata metadata) {
        log.info("recordLoginFailure invoked on thread={}", Thread.currentThread().getName());
        String key = buildAttemptKey(username, metadata);
        FailedLoginTracker tracker = failedLoginCache.get(key, k -> new FailedLoginTracker());
        int attempts = tracker.incrementAndGet();

        SecurityEvent event = baseEvent(SecurityEventType.LOGIN_FAILURE, metadata);
        event.setUsername(username);
        event.setDetails("attempts=" + attempts);
        securityEventRepository.save(event);

        if (attempts >= BRUTE_FORCE_THRESHOLD && tracker.markAlertEmitted()) {
            SecurityEvent alert = baseEvent(SecurityEventType.BRUTE_FORCE_ALERT, metadata);
            alert.setUsername(username);
            alert.setDetails("attempts=" + attempts);
            securityEventRepository.save(alert);
        }
    }

    @Async
    @Override
    public void recordTokenIssued(User user, String token, RequestMetadata metadata) {
        log.info("recordTokenIssued invoked on thread={}", Thread.currentThread().getName());
        SecurityEvent event = baseEvent(SecurityEventType.TOKEN_ISSUED, metadata);
        event.setUserId(user.getId());
        event.setUsername(user.getUsername());
        event.setTokenHash(hashToken(token));
        securityEventRepository.save(event);
    }

    @Async
    @Override
    public void recordTokenValidated(String token, String username, Long userId, RequestMetadata metadata) {
        log.info("recordTokenValidated invoked on thread={}", Thread.currentThread().getName());
        SecurityEvent event = baseEvent(SecurityEventType.TOKEN_VALID, metadata);
        event.setUserId(userId);
        event.setUsername(username);
        event.setTokenHash(hashToken(token));
        securityEventRepository.save(event);
    }

    @Async
    @Override
    public void recordTokenRejected(String token, RequestMetadata metadata) {
        log.info("recordTokenRejected invoked on thread={}", Thread.currentThread().getName());
        SecurityEvent event = baseEvent(SecurityEventType.TOKEN_INVALID, metadata);
        event.setTokenHash(hashToken(token));
        securityEventRepository.save(event);
    }

    @Async
    @Override
    public void recordOAuth2Success(User user, RequestMetadata metadata) {
        log.info("recordOAuth2Success invoked on thread={}", Thread.currentThread().getName());
        SecurityEvent event = baseEvent(SecurityEventType.OAUTH2_SUCCESS, metadata);
        event.setUserId(user.getId());
        event.setUsername(user.getUsername());
        securityEventRepository.save(event);
    }

    private SecurityEvent baseEvent(SecurityEventType type, RequestMetadata metadata) {
        SecurityEvent event = new SecurityEvent();
        event.setEventType(type);
        event.setIpAddress(metadata.getIpAddress());
        event.setUserAgent(truncate(metadata.getUserAgent(), 512));
        event.setEndpoint(truncate(metadata.getEndpoint(), 200));
        return event;
    }

    private String buildAttemptKey(String username, RequestMetadata metadata) {
        String userPart = Objects.requireNonNullElse(username, "unknown");
        return userPart + "|" + metadata.getIpAddress();
    }

    private String hashToken(String token) {
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

    private String truncate(String value, int maxLength) {
        if (value == null) return null;
        if (value.length() <= maxLength) return value;
        return value.substring(0, maxLength);
    }

    private static class FailedLoginTracker {
        private final AtomicInteger attempts = new AtomicInteger();
        private final AtomicBoolean alertEmitted = new AtomicBoolean(false);

        int incrementAndGet() {
            return attempts.incrementAndGet();
        }

        boolean markAlertEmitted() {
            return alertEmitted.compareAndSet(false, true);
        }
    }
}
