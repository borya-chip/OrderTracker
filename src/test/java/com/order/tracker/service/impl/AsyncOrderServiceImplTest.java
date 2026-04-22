package com.order.tracker.service.impl;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

import com.order.tracker.domain.AsyncTask;
import com.order.tracker.domain.AsyncTaskStatus;
import com.order.tracker.dto.request.OrderRequest;
import com.order.tracker.exception.BadRequestException;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.Set;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class AsyncOrderServiceImplTest {

    @Mock
    private AsyncTaskStorage asyncTaskStorage;

    @Mock
    private AsyncOrderExecutorService asyncOrderExecutor;

    private AsyncOrderServiceImpl service;

    @BeforeEach
    void setUp() {
        service = new AsyncOrderServiceImpl(asyncTaskStorage, asyncOrderExecutor);
    }

    @Test
    void createOrdersAsyncShouldSavePendingTaskAndStartExecution() {
        List<OrderRequest> requests = List.of(request("Coffee"));

        String taskId = service.createOrdersAsync(requests, true);

        ArgumentCaptor<AsyncTask> taskCaptor = ArgumentCaptor.forClass(AsyncTask.class);
        verify(asyncTaskStorage).saveTask(taskCaptor.capture());
        AsyncTask savedTask = taskCaptor.getValue();

        assertEquals(taskId, savedTask.getTaskId());
        assertEquals(AsyncTaskStatus.PENDING, savedTask.getStatus());
        assertNotNull(savedTask.getStartTime());
        assertEquals(0, savedTask.getProgress());
        verify(asyncOrderExecutor).executeOrdersCreation(taskId, requests, true);
    }

    @Test
    void createOrdersAsyncShouldRejectNullRequests() {
        BadRequestException exception = assertThrows(
                BadRequestException.class,
                () -> service.createOrdersAsync(null, false));

        assertTrue(exception.getMessage().contains("at least one item"));
        verifyNoInteractions(asyncTaskStorage, asyncOrderExecutor);
    }

    @Test
    void createOrdersAsyncShouldRejectEmptyRequests() {
        List<OrderRequest> requests = List.of();

        BadRequestException exception = assertThrows(
                BadRequestException.class,
                () -> service.createOrdersAsync(requests, false));

        assertTrue(exception.getMessage().contains("at least one item"));
        verifyNoInteractions(asyncTaskStorage, asyncOrderExecutor);
    }

    @Test
    void getOrderTaskStatusShouldReturnValueFromStorage() {
        AsyncTask task = AsyncTask.builder().taskId("task-1").status(AsyncTaskStatus.COMPLETED).build();
        when(asyncTaskStorage.getTask("task-1")).thenReturn(task);

        AsyncTask result = service.getOrderTaskStatus("task-1");

        assertSame(task, result);
    }

    @Test
    void getAllAsyncTasksShouldReturnStorageSnapshot() {
        Map<String, AsyncTask> tasks = Map.of(
                "task-1",
                AsyncTask.builder().taskId("task-1").status(AsyncTaskStatus.PENDING).build());
        when(asyncTaskStorage.getAllTasks()).thenReturn(tasks);

        Map<String, AsyncTask> result = service.getAllAsyncTasks();

        assertSame(tasks, result);
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
