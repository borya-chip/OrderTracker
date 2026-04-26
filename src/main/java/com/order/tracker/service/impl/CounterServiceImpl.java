package com.order.tracker.service.impl;

import com.order.tracker.service.CounterService;
import java.util.concurrent.atomic.AtomicInteger;
import org.springframework.stereotype.Service;

@Service
public class CounterServiceImpl implements CounterService {

    private final AtomicInteger atomicCounter = new AtomicInteger(0);

    @Override
    public void increment() {
        atomicCounter.incrementAndGet();
    }

    @Override
    public int getValue() {
        return atomicCounter.get();
    }

    @Override
    public void reset() {
        atomicCounter.set(0);
    }
}
