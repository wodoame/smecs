package com.smecs.service.impl;

import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.Caffeine;
import com.smecs.entity.SecurityEvent;
import com.smecs.entity.SecurityEventType;
import com.smecs.entity.User;
import com.smecs.repository.SecurityEventRepository;
import com.smecs.service.SecurityEventService;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Autowired;
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

    @Override
    public void recordLoginSuccess(User user, HttpServletRequest request) {
        SecurityEvent event = baseEvent(SecurityEventType.LOGIN_SUCCESS, request);
        event.setUserId(user.getId());
        event.setUsername(user.getUsername());
        securityEventRepository.save(event);
    }

    @Override
    public void recordLoginFailure(String username, HttpServletRequest request) {
        String key = buildAttemptKey(username, request);
        FailedLoginTracker tracker = failedLoginCache.get(key, k -> new FailedLoginTracker());
        int attempts = tracker.incrementAndGet();

        SecurityEvent event = baseEvent(SecurityEventType.LOGIN_FAILURE, request);
        event.setUsername(username);
        event.setDetails("attempts=" + attempts);
        securityEventRepository.save(event);

        if (attempts >= BRUTE_FORCE_THRESHOLD && tracker.markAlertEmitted()) {
            SecurityEvent alert = baseEvent(SecurityEventType.BRUTE_FORCE_ALERT, request);
            alert.setUsername(username);
            alert.setDetails("attempts=" + attempts);
            securityEventRepository.save(alert);
        }
    }

    @Override
    public void recordTokenIssued(User user, String token, HttpServletRequest request) {
        SecurityEvent event = baseEvent(SecurityEventType.TOKEN_ISSUED, request);
        event.setUserId(user.getId());
        event.setUsername(user.getUsername());
        event.setTokenHash(hashToken(token));
        securityEventRepository.save(event);
    }

    @Override
    public void recordTokenValidated(String token, String username, Long userId, HttpServletRequest request) {
        SecurityEvent event = baseEvent(SecurityEventType.TOKEN_VALID, request);
        event.setUserId(userId);
        event.setUsername(username);
        event.setTokenHash(hashToken(token));
        securityEventRepository.save(event);
    }

    @Override
    public void recordTokenRejected(String token, HttpServletRequest request) {
        SecurityEvent event = baseEvent(SecurityEventType.TOKEN_INVALID, request);
        event.setTokenHash(hashToken(token));
        securityEventRepository.save(event);
    }

    @Override
    public void recordOAuth2Success(User user, HttpServletRequest request) {
        SecurityEvent event = baseEvent(SecurityEventType.OAUTH2_SUCCESS, request);
        event.setUserId(user.getId());
        event.setUsername(user.getUsername());
        securityEventRepository.save(event);
    }

    private SecurityEvent baseEvent(SecurityEventType type, HttpServletRequest request) {
        SecurityEvent event = new SecurityEvent();
        event.setEventType(type);
        event.setIpAddress(extractClientIp(request));
        event.setUserAgent(truncate(request.getHeader("User-Agent"), 512));
        event.setEndpoint(truncate(request.getRequestURI(), 200));
        return event;
    }

    private String buildAttemptKey(String username, HttpServletRequest request) {
        String userPart = Objects.requireNonNullElse(username, "unknown");
        return userPart + "|" + extractClientIp(request);
    }

    private String extractClientIp(HttpServletRequest request) {
        String forwarded = request.getHeader("X-Forwarded-For");
        if (forwarded != null && !forwarded.isBlank()) {
            return forwarded.split(",")[0].trim();
        }
        return request.getRemoteAddr();
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

