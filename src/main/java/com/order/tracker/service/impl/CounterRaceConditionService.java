package com.order.tracker.service.impl;

import java.util.concurrent.atomic.AtomicInteger;
import org.springframework.stereotype.Service;

@Service
public class CounterRaceConditionService {

    private int unsafeCounter;
    private final AtomicInteger atomicCounter = new AtomicInteger();

    public void resetAll() {
        unsafeCounter = 0;
        atomicCounter.set(0);
    }

    public void incrementUnsafe() {
        unsafeCounter++;
    }

    public void incrementAtomic() {
        atomicCounter.incrementAndGet();
    }

    public int getUnsafeCounter() {
        return unsafeCounter;
    }

    public int getAtomicCounter() {
        return atomicCounter.get();
    }
}
