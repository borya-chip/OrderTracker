package com.order.tracker.service.impl;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class CounterRaceConditionServiceTest {

    private CounterRaceConditionService service;

    @BeforeEach
    void setUp() {
        service = new CounterRaceConditionService();
    }

    @Test
    void resetAllShouldClearAllCounters() {
        service.incrementUnsafe();
        service.incrementAtomic();

        service.resetAll();

        assertEquals(0, service.getUnsafeCounter());
        assertEquals(0, service.getAtomicCounter());
    }

    @Test
    void directIncrementMethodsShouldIncreaseOwnCounters() {
        service.incrementUnsafe();
        service.incrementUnsafe();
        service.incrementAtomic();

        assertEquals(2, service.getUnsafeCounter());
        assertEquals(1, service.getAtomicCounter());
    }
}
