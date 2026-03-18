package com.smecs.aop;

import com.smecs.config.CacheConfig;
import com.smecs.dto.CategoryQuery;
import com.smecs.dto.OrderQuery;
import com.smecs.dto.ProductQuery;
import com.smecs.dto.InventoryQuery;
import com.smecs.service.impl.CategoryServiceImpl;
import com.smecs.service.impl.OrderServiceImpl;
import com.smecs.service.impl.ProductServiceImpl;
import com.smecs.service.impl.InventoryServiceImpl;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Pointcut;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.cache.Cache;
import org.springframework.cache.CacheManager;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

@Aspect
@Component
@Order(Ordered.HIGHEST_PRECEDENCE)
public class ServiceLoggingAspect {
    private static final Logger logger = LoggerFactory.getLogger(ServiceLoggingAspect.class);
    private final CacheManager cacheManager;
    private final boolean cacheEnabled;

    @Autowired
    public ServiceLoggingAspect(CacheManager cacheManager, @Value("${app.cache.enabled:true}") boolean cacheEnabled) {
        this.cacheManager = cacheManager;
        this.cacheEnabled = cacheEnabled;
    }

    @Pointcut("execution(* com.smecs.service.ProductService.getProducts(..)) || " +
            "execution(* com.smecs.service.InventoryService.searchInventory(..)) || " +
            "execution(* com.smecs.service.CategoryService.getCategories(..)) || " +
            "execution(* com.smecs.service.OrderService.getOrderById(..)) || " +
            "execution(* com.smecs.service.OrderService.getAllOrders(..)) || " +
            "execution(* com.smecs.service.OrderService.getOrdersByUserId(..))")
    public void serviceLayer() {
        // Pointcut for specific service layer methods.
    }

    @Around("serviceLayer()")
    public Object logAround(ProceedingJoinPoint joinPoint) throws Throwable {
        long startTime = System.currentTimeMillis();
        try {
            return joinPoint.proceed();
        } finally {
            long durationMs = System.currentTimeMillis() - startTime;
            logger.info("{} executed in {} ms", joinPoint.getSignature(), durationMs);
        }
    }

    @Pointcut("execution(* com.smecs.service.impl.ProductServiceImpl.getProductById(..)) || " +
            "execution(* com.smecs.service.impl.ProductServiceImpl.getProducts(com.smecs.dto.ProductQuery)) || " +
            "execution(* com.smecs.service.impl.CategoryServiceImpl.getCategoryById(..)) || " +
            "execution(* com.smecs.service.impl.CategoryServiceImpl.getCategories(com.smecs.dto.CategoryQuery)) || " +
            "execution(* com.smecs.service.impl.OrderServiceImpl.getOrderById(..)) || " +
            "execution(* com.smecs.service.impl.OrderServiceImpl.getAllOrders(com.smecs.dto.OrderQuery)) || " +
            "execution(* com.smecs.service.impl.OrderServiceImpl.getOrdersByUserId(..)) || " +
            "execution(* com.smecs.service.impl.InventoryServiceImpl.getInventoryById(..)) || " +
            "execution(* com.smecs.service.impl.InventoryServiceImpl.getInventoryByProductId(..)) || " +
            "execution(* com.smecs.service.impl.InventoryServiceImpl.searchInventory(com.smecs.dto.InventoryQuery))")
    public void cacheableLookups() {
        // Pointcut for cacheable lookups with explicit key behavior.
    }

    @Around("cacheableLookups()")
    public Object logCacheHitOrMiss(ProceedingJoinPoint joinPoint) throws Throwable {
        // If caching is disabled at runtime, skip cache hit/miss logging entirely.
        if (!cacheEnabled) {
            return joinPoint.proceed();
        }

        CacheTarget target = resolveCacheTarget(joinPoint);
        if (target != null) {
            logCacheStatus(target.cacheName, target.key, target.keyForLog);
        }
        return joinPoint.proceed();
    }

    private CacheTarget resolveCacheTarget(JoinPoint joinPoint) {
        Object target = joinPoint.getTarget();
        Object[] args = joinPoint.getArgs();
        String methodName = joinPoint.getSignature().getName();

        if (target instanceof ProductServiceImpl) {
            if ("getProductById".equals(methodName) && args.length >= 1) {
                Object id = args[0];
                return CacheTarget.of(CacheConfig.PRODUCTS_BY_ID, id, String.valueOf(id));
            }
            if ("getProducts".equals(methodName) && args.length == 1 && args[0] instanceof ProductQuery) {
                String key = ProductServiceImpl.searchCacheKey((ProductQuery) args[0]);
                return CacheTarget.of(CacheConfig.PRODUCT_SEARCH, key, key);
            }
        }

        if (target instanceof CategoryServiceImpl) {
            if ("getCategoryById".equals(methodName) && args.length >= 2) {
                Number id = args[0] instanceof Number ? (Number) args[0] : null;
                boolean includeRelatedImages = args[1] instanceof Boolean && (Boolean) args[1];
                String key = CategoryServiceImpl.categoryByIdKey(id, includeRelatedImages);
                return CacheTarget.of(CacheConfig.CATEGORIES_BY_ID, key, key);
            }
            if ("getCategories".equals(methodName) && args.length == 1 && args[0] instanceof CategoryQuery) {
                String key = CategoryServiceImpl.searchCacheKey((CategoryQuery) args[0]);
                return CacheTarget.of(CacheConfig.CATEGORY_SEARCH, key, key);
            }
        }

        if (target instanceof OrderServiceImpl) {
            if ("getOrderById".equals(methodName) && args.length >= 1) {
                Object id = args[0];
                return CacheTarget.of(CacheConfig.ORDERS_BY_ID, id, String.valueOf(id));
            }
            if ("getAllOrders".equals(methodName) && args.length == 1 && args[0] instanceof OrderQuery) {
                String key = OrderServiceImpl.searchCacheKey((OrderQuery) args[0]);
                return CacheTarget.of(CacheConfig.ORDER_SEARCH, key, key);
            }
            if ("getOrdersByUserId".equals(methodName) && args.length >= 2 && args[1] instanceof OrderQuery) {
                Long userId = args[0] instanceof Long ? (Long) args[0] : null;
                String key = OrderServiceImpl.userSearchCacheKey(userId, (OrderQuery) args[1]);
                return CacheTarget.of(CacheConfig.USER_ORDER_SEARCH, key, key);
            }
        }

        if (target instanceof InventoryServiceImpl) {
            if ("getInventoryById".equals(methodName) && args.length >= 1) {
                Object id = args[0];
                return CacheTarget.of(CacheConfig.INVENTORIES_BY_ID, id, String.valueOf(id));
            }
            if ("getInventoryByProductId".equals(methodName) && args.length >= 1) {
                Object productId = args[0];
                return CacheTarget.of(CacheConfig.INVENTORIES_BY_PRODUCT_ID, productId, String.valueOf(productId));
            }
            if ("searchInventory".equals(methodName) && args.length == 1 && args[0] instanceof InventoryQuery) {
                String key = InventoryServiceImpl.searchCacheKey((InventoryQuery) args[0]);
                return CacheTarget.of(CacheConfig.INVENTORY_SEARCH, key, key);
            }
        }

        return null;
    }

    private void logCacheStatus(String cacheName, Object key, String keyForLog) {
        Cache cache = cacheManager.getCache(cacheName);
        if (cache == null) {
            logger.debug("Cache not configured: {}", cacheName);
            return;
        }

        boolean hit = cache.get(key) != null;
        if (hit) {
            logger.info("Cache hit for key: {} (cache: {})", keyForLog, cacheName);
        } else {
            logger.info("Cache miss for key: {} (cache: {})", keyForLog, cacheName);
        }
    }

    private record CacheTarget(String cacheName, Object key, String keyForLog) {

        private static CacheTarget of(String cacheName, Object key, String keyForLog) {
                return new CacheTarget(cacheName, key, keyForLog);
            }
        }
}
