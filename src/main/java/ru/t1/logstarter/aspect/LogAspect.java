package ru.t1.logstarter.aspect;

import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.*;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import ru.t1.logstarter.configuration.LogProperties;

import java.util.Arrays;

@Aspect
public class LogAspect {
    private final Logger logger;
    private final LogProperties LogProperties;

    public LogAspect(LogProperties LogProperties) {
        this.logger = LoggerFactory.getLogger(LogAspect.class);
        this.LogProperties = LogProperties;
    }

    @Pointcut("@annotation(ru.t1.logstarter.aspect.annotation.LogExecution)")
    public void logMethods() {
    }

    @Before("logMethods()")
    public void logBefore(JoinPoint jp) {
        Object[] args = jp.getArgs();
        logAtLevel("Method {}.{}() executed with {} arguments",
                jp.getSignature().getDeclaringTypeName(),
                jp.getSignature().getName(),
                args.length > 0 ? Arrays.toString(args) : "no");
    }

    @AfterReturning(pointcut = "logMethods()", returning = "result")
    public void logAfterReturning(JoinPoint jp, Object result) {
        String className = jp.getSignature().getDeclaringTypeName();
        String methodName = jp.getSignature().getName();
        if (result != null) logAtLevel("Method {}.{}() returned: {}", className, methodName, result);
        else logAtLevel("Method {}.{}() returned no value (void)", className, methodName);
    }

    @AfterThrowing(pointcut = "logMethods()", throwing = "ex")
    public void logAfterThrowing(JoinPoint joinPoint, Throwable ex) {
        logger.error("Exception in {}.{}() with message = '{}' and cause = {}",
                joinPoint.getSignature().getDeclaringTypeName(),
                joinPoint.getSignature().getName(),
                ex.getMessage(),
                ex.getCause() != null ? ex.getCause() : "NULL");
    }

    @Around("@annotation(ru.t1.logstarter.aspect.annotation.TimeExecutionTracking)")
    public Object trackAround(ProceedingJoinPoint pjp) {
        try {
            long startTime = System.currentTimeMillis();
            Object result = pjp.proceed();
            long endTime = System.currentTimeMillis();
            logAtLevel("Method {}.{}() execution time: {} ms",
                    pjp.getSignature().getDeclaringTypeName(), pjp.getSignature().getName(), endTime - startTime);
            return result;
        } catch (Throwable ex) {
            throw new RuntimeException("An exception occurred in the method during execution %s.%s()"
                    .formatted(pjp.getSignature().getDeclaringTypeName(), pjp.getSignature().getName()), ex);
        }
    }

    private void logAtLevel(String message, Object... args) {
        logger.atLevel(LogProperties.logLevel()).log(message, args);
    }
}
