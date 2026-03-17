package com.smecs.config;

import com.github.benmanes.caffeine.cache.Caffeine;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.cache.CacheManager;
import org.springframework.cache.caffeine.CaffeineCacheManager;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import jakarta.annotation.PostConstruct;

import java.time.Duration;

@Configuration
@ConditionalOnProperty(name = "app.cache.enabled", havingValue = "true", matchIfMissing = true)
public class CacheConfig {
    private static final Logger log = LoggerFactory.getLogger(CacheConfig.class);

    public static final String PRODUCTS_BY_ID = "productsById";
    public static final String PRODUCT_SEARCH = "productSearch";
    public static final String CATEGORIES_BY_ID = "categoriesById";
    public static final String CATEGORY_SEARCH = "categorySearch";
    public static final String ORDERS_BY_ID = "ordersById";
    public static final String ORDER_SEARCH = "orderSearch";
    public static final String USER_ORDER_SEARCH = "userOrderSearch";
    public static final String INVENTORIES_BY_ID = "inventoriesById";
    public static final String INVENTORIES_BY_PRODUCT_ID = "inventoriesByProductId";
    public static final String INVENTORY_SEARCH = "inventorySearch";

    private static final long DEFAULT_TTL_MINUTES = 5;
    private static final long DEFAULT_MAX_SIZE = 1_000;

    @PostConstruct
    public void started() {
        log.info("CacheConfig active: app.cache.enabled=true (caching enabled)");
    }

    @Bean
    public CacheManager cacheManager() {
        CaffeineCacheManager manager = new CaffeineCacheManager(
                PRODUCTS_BY_ID,
                PRODUCT_SEARCH,
                CATEGORIES_BY_ID,
                CATEGORY_SEARCH,
                ORDERS_BY_ID,
                ORDER_SEARCH,
                USER_ORDER_SEARCH,
                INVENTORIES_BY_ID,
                INVENTORIES_BY_PRODUCT_ID,
                INVENTORY_SEARCH
        );
        manager.setCaffeine(
                Caffeine.newBuilder()
                        .expireAfterWrite(Duration.ofMinutes(DEFAULT_TTL_MINUTES))
                        .maximumSize(DEFAULT_MAX_SIZE)
        );
        return manager;
    }
}
