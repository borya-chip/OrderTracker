package com.order.tracker.service;

import com.order.tracker.dto.response.TransactionDemoResponse;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

@SpringBootTest
class TransactionDemoServiceIntegrationTest {

    @Autowired
    private TransactionDemoService transactionDemoService;

    @Test
    void shouldPartiallyPersistDataWithoutTransactionalBoundary() {
        TransactionDemoResponse response = transactionDemoService.runWithoutTransactional();

        assertNotNull(response.getError());
        assertTrue(response.getCustomerDelta() > 0);
        assertTrue(response.getCategoryDelta() > 0);
        assertTrue(response.getRestaurantDelta() > 0);
        assertTrue(response.getMealDelta() > 0);
        assertTrue(response.getOrderDelta() > 0);
    }

    @Test
    void shouldRollbackWholeOperationWithTransactionalBoundary() {
        TransactionDemoResponse response = transactionDemoService.runWithTransactional();

        assertNotNull(response.getError());
        assertEquals(0L, response.getCustomerDelta());
        assertEquals(0L, response.getCategoryDelta());
        assertEquals(0L, response.getRestaurantDelta());
        assertEquals(0L, response.getMealDelta());
        assertEquals(0L, response.getOrderDelta());
    }
}
