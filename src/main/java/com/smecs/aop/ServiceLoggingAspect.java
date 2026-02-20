package com.smecs.aop;

import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.AfterReturning;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Pointcut;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.util.Optional;

@Aspect
@Component
public class ServiceLoggingAspect {
    private static final Logger logger = LoggerFactory.getLogger(ServiceLoggingAspect.class);

    @Pointcut("execution(* com.smecs.service.ProductService.getProducts(..)) || " +
            "execution(* com.smecs.service.InventoryService.searchInventory(..)) || " +
            "execution(* com.smecs.service.impl.InventoryCacheService.getSearchResults(..)) || " +
            "execution(* com.smecs.service.CategoryService.getCategories(..)) || " +
            "execution(* com.smecs.service.impl.CategoryCacheService.getSearchResults(..))")
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

    @AfterReturning(pointcut = "execution(* com.smecs.service.impl.InventoryCacheService.getSearchResults(..)) || " +
            "execution(* com.smecs.service.impl.CategoryCacheService.getSearchResults(..))", returning = "result")
    public void logCacheHit(JoinPoint joinPoint, Object result) {
        if (result instanceof Optional) {
            Object[] args = joinPoint.getArgs();
            String key;
            if (args.length >= 4) {
                key = String.format("%s|%s|%s|%s", args[0], args[1], args[2], args[3]);
            } else {
                key = "unknown";
            }

            if (((Optional<?>) result).isPresent()) {
                logger.info("Search cache hit for key: {}", key);
            } else {
                logger.info("Search cache miss for key: {}", key);
            }
        }
    }
}
