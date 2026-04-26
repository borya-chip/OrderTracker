package com.order.tracker.service.impl;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.order.tracker.dto.response.RaceConditionDemoResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class RaceConditionDemoServiceTest {

    private RaceConditionDemoService service;

    @BeforeEach
    void setUp() {
        service = new RaceConditionDemoService(new CounterRaceConditionService());
    }

    @Test
    void runAllDemosShouldCompareUnsafeAndAtomicCounters() throws InterruptedException {
        RaceConditionDemoResponse response = service.runAllDemos();

        assertEquals(64, response.getThreadCount());
        assertEquals(10_000, response.getIncrementsPerThread());
        assertEquals(640_000, response.getExpectedValue());

        assertEquals("Unsafe counter", response.getUnsafeCounter().getName());
        assertTrue(response.getUnsafeCounter().getActualValue() <= response.getExpectedValue());
        assertEquals(
                response.getExpectedValue() - response.getUnsafeCounter().getActualValue(),
                response.getUnsafeCounter().getLostUpdates());

        assertEquals("Atomic counter", response.getAtomicCounter().getName());
        assertTrue(response.getAtomicCounter().isMatchesExpected());
        assertEquals(0, response.getAtomicCounter().getLostUpdates());
    }

}
