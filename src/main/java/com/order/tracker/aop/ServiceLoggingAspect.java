package com.order.tracker.aop;

import java.util.Arrays;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Pointcut;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.springframework.util.StopWatch;

@Aspect
@Component
public class ServiceLoggingAspect {

    private static final int SLOW_THRESHOLD_MS = 500;
    private static final int VERY_SLOW_THRESHOLD_MS = 1000;

    @Pointcut("within(@org.springframework.stereotype.Service *)")
    public void serviceMethods() {
    }

    @Around("serviceMethods()")
    public Object logExecutionTime(final ProceedingJoinPoint joinPoint) throws Throwable {
        Logger logger = LoggerFactory.getLogger(joinPoint.getTarget().getClass());
        String className = joinPoint.getTarget().getClass().getSimpleName();
        String methodName = joinPoint.getSignature().getName();
        String fullMethodName = className + "." + methodName;

        StopWatch stopWatch = new StopWatch(fullMethodName);
        stopWatch.start(fullMethodName);

        if (logger.isDebugEnabled()) {
            logger.debug("Executing method: {} with arguments: {}",
                    fullMethodName,
                    Arrays.toString(joinPoint.getArgs()));
        }

        try {
            Object result = joinPoint.proceed();
            stopWatch.stop();
            long executionTime = stopWatch.getTotalTimeMillis();

            if (executionTime > VERY_SLOW_THRESHOLD_MS) {
                logger.warn("Method {} completed in {} ms (exceeds {} ms threshold)",
                        fullMethodName,
                        executionTime,
                        VERY_SLOW_THRESHOLD_MS);
            } else if (executionTime > SLOW_THRESHOLD_MS) {
                logger.info("Method {} completed in {} ms", fullMethodName, executionTime);
            } else {
                logger.debug("Method {} completed in {} ms", fullMethodName, executionTime);
            }

            return result;
        } catch (RuntimeException | Error exception) {
            if (stopWatch.isRunning()) {
                stopWatch.stop();
            }
            throw exception;
        } catch (Throwable throwable) {
            if (stopWatch.isRunning()) {
                stopWatch.stop();
            }
            logger.error("Method {} failed in {} ms",
                    fullMethodName,
                    stopWatch.getTotalTimeMillis(),
                    throwable);
            throw new IllegalStateException("Unexpected exception while executing " + fullMethodName, throwable);
        }
    }
}
