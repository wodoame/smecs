package com.smecs.aop;

import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.After;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Before;
import org.aspectj.lang.annotation.Pointcut;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

@Aspect
@Component
public class ServiceLoggingAspect {
    private static final Logger logger = LoggerFactory.getLogger(ServiceLoggingAspect.class);

    @Pointcut("execution(* com.smecs.service..*(..))")
    public void serviceLayer() {
        // Pointcut for service layer methods.
    }

    @Before("serviceLayer()")
    public void logBefore(JoinPoint joinPoint) {
        logger.info("Entering {}", joinPoint.getSignature());
    }

    @After("serviceLayer()")
    public void logAfter(JoinPoint joinPoint) {
        logger.info("Exiting {}", joinPoint.getSignature());
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
}
