package com.order.tracker.service.impl;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class CounterServiceImplTest {

    private CounterServiceImpl service;

    @BeforeEach
    void setUp() {
        service = new CounterServiceImpl();
    }

    @Test
    void incrementShouldIncreaseAtomicCounter() {
        service.increment();
        service.increment();

        assertEquals(2, service.getValue());
    }

    @Test
    void resetShouldClearAtomicCounter() {
        service.increment();

        service.reset();

        assertEquals(0, service.getValue());
    }
}
