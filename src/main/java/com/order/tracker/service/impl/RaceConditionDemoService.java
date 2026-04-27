package com.order.tracker.service.impl;

import com.order.tracker.dto.response.RaceConditionDemoResponse;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class RaceConditionDemoService {

    private static final int THREAD_COUNT = 50;
    private static final int INCREMENTS_PER_THREAD = 1000;
    private static final int EXPECTED_VALUE = THREAD_COUNT * INCREMENTS_PER_THREAD;

    private final CounterRaceConditionService counterService;

    public RaceConditionDemoResponse runAllDemos() throws InterruptedException {
        counterService.resetAll();
        runConcurrentIncrements(counterService::incrementUnsafe);
        RaceConditionDemoResponse.CounterResult unsafeResult =
                buildCounterResult("Unsafe counter", counterService.getUnsafeCounter());

        counterService.resetAll();
        runConcurrentIncrements(counterService::incrementAtomic);
        RaceConditionDemoResponse.CounterResult atomicResult =
                buildCounterResult("Atomic counter", counterService.getAtomicCounter());

        return new RaceConditionDemoResponse(
                THREAD_COUNT,
                INCREMENTS_PER_THREAD,
                EXPECTED_VALUE,
                unsafeResult,
                atomicResult);
    }

    private void runConcurrentIncrements(final Runnable incrementAction) throws InterruptedException {
        ExecutorService executorService = Executors.newFixedThreadPool(THREAD_COUNT);
        CountDownLatch startLatch = new CountDownLatch(1);
        CountDownLatch doneLatch = new CountDownLatch(THREAD_COUNT);

        try {
            for (int i = 0; i < THREAD_COUNT; i++) {
                executorService.submit(() -> {
                    await(startLatch);
                    for (int j = 0; j < INCREMENTS_PER_THREAD; j++) {
                        incrementAction.run();
                    }
                    doneLatch.countDown();
                });
            }

            startLatch.countDown();
            if (!doneLatch.await(30, TimeUnit.SECONDS)) {
                throw new IllegalStateException("Race condition demo did not finish within 30 seconds");
            }
        } finally {
            executorService.shutdownNow();
        }
    }

    private void await(final CountDownLatch latch) {
        try {
            latch.await();
        } catch (InterruptedException exception) {
            Thread.currentThread().interrupt();
            throw new IllegalStateException(exception.getMessage(), exception);
        }
    }

    private RaceConditionDemoResponse.CounterResult buildCounterResult(
            final String counterName,
            final int actualValue) {
        int lostUpdates = EXPECTED_VALUE - actualValue;
        boolean matchesExpected = actualValue == EXPECTED_VALUE;
        String verdict = matchesExpected ? "OK" : "RACE CONDITION";

        return new RaceConditionDemoResponse.CounterResult(
                counterName,
                actualValue,
                lostUpdates,
                matchesExpected,
                verdict);
    }
}
