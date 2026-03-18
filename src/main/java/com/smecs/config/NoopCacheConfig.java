package com.smecs.config;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.cache.CacheManager;
import org.springframework.cache.support.NoOpCacheManager;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import jakarta.annotation.PostConstruct;

/**
 * Provides a no-op CacheManager when caching is explicitly disabled.
 * This prevents other beans (for example logging aspects) from failing
 * to start when they depend on a CacheManager, while ensuring no
 * cache hits/misses are recorded.
 */
@Configuration
@ConditionalOnProperty(name = "app.cache.enabled", havingValue = "false")
public class NoopCacheConfig {
    private static final Logger log = LoggerFactory.getLogger(NoopCacheConfig.class);

    @PostConstruct
    public void started() {
        log.info("NoopCacheConfig active: app.cache.enabled=false (caching disabled)");
    }

    @Bean
    public CacheManager cacheManager() {
        return new NoOpCacheManager();
    }
}
