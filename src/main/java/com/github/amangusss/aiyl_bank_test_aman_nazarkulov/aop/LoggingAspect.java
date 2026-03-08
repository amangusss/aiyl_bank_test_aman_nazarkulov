package com.github.amangusss.aiyl_bank_test_aman_nazarkulov.aop;

import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.slf4j.MDC;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import java.util.UUID;

@Slf4j
@Aspect
@Component
public class LoggingAspect {

    public static final String TRANSACTION_ID_KEY = "transactionId";

    @Around("@annotation(com.github.amangusss.aiyl_bank_test_aman_nazarkulov.aop.HttpLog)")
    public Object logHttp(ProceedingJoinPoint joinPoint) throws Throwable {
        String transactionId = UUID.randomUUID().toString();
        MDC.put(TRANSACTION_ID_KEY, transactionId);

        ServletRequestAttributes attrs = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
        String method = attrs != null ? attrs.getRequest().getMethod() : "UNKNOWN";
        String uri = attrs != null ? attrs.getRequest().getRequestURI() : "UNKNOWN";

        log.info("[Transaction: {}] -> {} {}", transactionId, method, uri);

        try {
            Object result = joinPoint.proceed();
            log.info("[Transaction: {}] 200 OK", transactionId);
            return result;
        } catch (Exception ex) {
            log.error("[Transaction: {}] ERROR: {}", transactionId, ex.getMessage());
            throw ex;
        } finally {
            MDC.remove(TRANSACTION_ID_KEY);
        }
    }

    @Around("@annotation(com.github.amangusss.aiyl_bank_test_aman_nazarkulov.aop.TransactionLog)")
    public Object logTransaction(ProceedingJoinPoint joinPoint) throws Throwable {
        String transactionId = MDC.get(TRANSACTION_ID_KEY);
        if (transactionId == null) {
            transactionId = UUID.randomUUID().toString();
        }

        String className = joinPoint.getSignature().getDeclaringType().getSimpleName();
        String methodName = joinPoint.getSignature().getName();

        log.info("[Transaction: {}] Entering {}.{}", transactionId, className, methodName);

        try {
            Object result = joinPoint.proceed();
            log.info("[Transaction: {}] Completed {}.{}", transactionId, className, methodName);
            return result;
        } catch (Exception ex) {
            log.error("[Transaction: {}] Failed {}.{}: {}", transactionId, className, methodName, ex.getMessage());
            throw ex;
        }
    }
}
