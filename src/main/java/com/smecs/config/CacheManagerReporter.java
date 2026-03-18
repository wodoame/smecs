package com.smecs.config;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.cache.CacheManager;
import org.springframework.stereotype.Component;

@Component
public class CacheManagerReporter implements CommandLineRunner {
    private static final Logger log = LoggerFactory.getLogger(CacheManagerReporter.class);

    private final CacheManager cacheManager;
    private final boolean cacheEnabled;

    @Autowired
    public CacheManagerReporter(CacheManager cacheManager, @Value("${app.cache.enabled:true}") boolean cacheEnabled) {
        this.cacheManager = cacheManager;
        this.cacheEnabled = cacheEnabled;
    }

    @Override
    public void run(String... args) {
        if (cacheManager != null) {
            log.info("app.cache.enabled={} -> CacheManager implementation: {}", cacheEnabled, cacheManager.getClass().getName());
        } else {
            log.info("app.cache.enabled={} -> No CacheManager bean present", cacheEnabled);
        }
    }
}
