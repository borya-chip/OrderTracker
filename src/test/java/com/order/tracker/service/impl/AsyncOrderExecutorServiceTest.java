package com.order.tracker.service.impl;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

import com.order.tracker.domain.AsyncTask;
import com.order.tracker.domain.AsyncTaskStatus;
import com.order.tracker.dto.request.OrderRequest;
import com.order.tracker.service.OrderService;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Set;
import java.util.concurrent.CompletableFuture;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.support.SimpleTransactionStatus;

@ExtendWith(MockitoExtension.class)
class AsyncOrderExecutorServiceTest {

    @Mock
    private AsyncTaskStorage asyncTaskStorage;

    @Mock
    private OrderService orderService;

    @Mock
    private PlatformTransactionManager transactionManager;

    private AsyncOrderExecutorService service;

    @BeforeEach
    void setUp() {
        service = new AsyncOrderExecutorService(asyncTaskStorage, orderService, transactionManager, 0, 0);
    }

    @Test
    void executeOrdersCreationShouldReturnCompletedFutureWhenTaskIsMissing() {
        CompletableFuture<Void> result =
                service.executeOrdersCreation("missing", List.of(request("Coffee")), false);

        assertTrue(result.isDone());
        verify(asyncTaskStorage).getTask("missing");
        verifyNoInteractions(orderService, transactionManager);
    }

    @Test
    void executeOrdersCreationShouldCompleteEmptyNonTransactionalImport() {
        AsyncTask task = task("task-1");
        when(asyncTaskStorage.getTask("task-1")).thenReturn(task);

        CompletableFuture<Void> result = service.executeOrdersCreation("task-1", List.of(), false);

        assertTrue(result.isDone());
        assertEquals(AsyncTaskStatus.COMPLETED, task.getStatus());
        assertEquals(100, task.getProgress());
        assertEquals("Created 0 orders", task.getResult());
        assertNotNull(task.getEndTime());
        verifyNoInteractions(orderService, transactionManager);
    }

    @Test
    void executeOrdersCreationShouldCompleteNonTransactionalImportAfterProcessingRequests() {
        AsyncTask task = task("task-1");
        when(asyncTaskStorage.getTask("task-1")).thenReturn(task);

        CompletableFuture<Void> result =
                service.executeOrdersCreation("task-1", List.of(request("Coffee")), false);

        assertTrue(result.isDone());
        assertEquals(AsyncTaskStatus.COMPLETED, task.getStatus());
        assertEquals(100, task.getProgress());
        assertEquals("Created 1 orders", task.getResult());
        assertNotNull(task.getEndTime());
        verify(orderService).createOrder(any(OrderRequest.class));
        verifyNoInteractions(transactionManager);
    }

    @Test
    void executeOrdersCreationShouldCommitEmptyTransactionalImport() {
        AsyncTask task = task("task-1");
        when(asyncTaskStorage.getTask("task-1")).thenReturn(task);
        when(transactionManager.getTransaction(any())).thenReturn(new SimpleTransactionStatus());

        CompletableFuture<Void> result = service.executeOrdersCreation("task-1", List.of(), true);

        assertTrue(result.isDone());
        assertEquals(AsyncTaskStatus.COMPLETED, task.getStatus());
        assertEquals("Created 0 orders", task.getResult());
        verify(transactionManager).getTransaction(any());
        verify(transactionManager).commit(any());
        verify(transactionManager, never()).rollback(any());
        verifyNoInteractions(orderService);
    }

    @Test
    void executeOrdersCreationShouldCommitTransactionalImportAfterProcessingRequests() {
        AsyncTask task = task("task-1");
        when(asyncTaskStorage.getTask("task-1")).thenReturn(task);
        when(transactionManager.getTransaction(any())).thenReturn(new SimpleTransactionStatus());

        CompletableFuture<Void> result =
                service.executeOrdersCreation("task-1", List.of(request("Coffee")), true);

        assertTrue(result.isDone());
        assertEquals(AsyncTaskStatus.COMPLETED, task.getStatus());
        assertEquals(100, task.getProgress());
        assertEquals("Created 1 orders", task.getResult());
        assertNotNull(task.getEndTime());
        verify(orderService).createOrder(any(OrderRequest.class));
        verify(transactionManager).commit(any());
        verify(transactionManager, never()).rollback(any());
    }

    @Test
    void executeOrdersCreationShouldRollbackTransactionalImportOnFailure() {
        AsyncTask task = task("task-1");
        when(asyncTaskStorage.getTask("task-1")).thenReturn(task);
        when(transactionManager.getTransaction(any())).thenReturn(new SimpleTransactionStatus());
        when(orderService.createOrder(any(OrderRequest.class)))
                .thenThrow(new IllegalStateException("boom"));

        CompletableFuture<Void> result =
                service.executeOrdersCreation("task-1", List.of(request("Coffee")), true);

        assertTrue(result.isDone());
        assertEquals(AsyncTaskStatus.FAILED, task.getStatus());
        assertEquals("Error: boom", task.getResult());
        assertNotNull(task.getEndTime());
        verify(transactionManager).rollback(any());
        verify(transactionManager, never()).commit(any());
    }

    @Test
    void executeOrdersCreationShouldMarkTaskFailedWhenThreadIsInterrupted() {
        AsyncTask task = task("task-1");
        when(asyncTaskStorage.getTask("task-1")).thenReturn(task);

        try {
            Thread.currentThread().interrupt();

            CompletableFuture<Void> result =
                    service.executeOrdersCreation("task-1", List.of(request("Coffee")), false);

            assertTrue(result.isDone());
            assertEquals(AsyncTaskStatus.FAILED, task.getStatus());
            assertEquals("Task was interrupted while waiting", task.getResult());
            assertNotNull(task.getEndTime());
            assertTrue(Thread.currentThread().isInterrupted());
            verifyNoInteractions(orderService, transactionManager);
        } finally {
            Thread.interrupted();
        }
    }

    private AsyncTask task(final String taskId) {
        return AsyncTask.builder()
                .taskId(taskId)
                .status(AsyncTaskStatus.PENDING)
                .startTime(LocalDateTime.of(2026, 4, 5, 12, 0))
                .progress(0)
                .build();
    }

    private OrderRequest request(final String description) {
        return new OrderRequest(
                new BigDecimal("10.00"),
                LocalDateTime.of(2026, 4, 5, 12, 0),
                description,
                1L,
                Set.of(1L));
    }
}
