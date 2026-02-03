package com.smecs.util;

public record CacheEntry<T>(T value, long expiresAtMs) {
    public CacheEntry(T value, long expiresAtMs) {
        this.value = value;
        this.expiresAtMs = System.currentTimeMillis() + expiresAtMs;
    }

    public boolean isExpired() {
        return System.currentTimeMillis() > expiresAtMs;
    }
}
