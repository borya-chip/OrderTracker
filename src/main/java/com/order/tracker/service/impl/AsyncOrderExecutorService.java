package com.order.tracker.service.impl;

import com.order.tracker.domain.AsyncTask;
import com.order.tracker.domain.AsyncTaskStatus;
import com.order.tracker.dto.request.OrderRequest;
import com.order.tracker.service.OrderService;
import java.time.LocalDateTime;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.support.TransactionTemplate;

@Service
public class AsyncOrderExecutorService {

    private static final class AsyncProcessingInterruptedException extends RuntimeException {
        private AsyncProcessingInterruptedException() {
            super("Async order import interrupted");
        }
    }

    private final AsyncTaskStorage asyncTaskStorage;
    private final OrderService orderService;
    private final PlatformTransactionManager transactionManager;
    private final long startupDelayMillis;
    private final long perOrderDelayMillis;

    public AsyncOrderExecutorService(
            final AsyncTaskStorage asyncTaskStorage,
            final OrderService orderService,
            final PlatformTransactionManager transactionManager,
            @Value("${app.async.orders.startup-delay-ms:0}") final long startupDelayMillis,
            @Value("${app.async.orders.per-order-delay-ms:0}") final long perOrderDelayMillis) {
        this.asyncTaskStorage = asyncTaskStorage;
        this.orderService = orderService;
        this.transactionManager = transactionManager;
        this.startupDelayMillis = startupDelayMillis;
        this.perOrderDelayMillis = perOrderDelayMillis;
    }

    @Async
    public CompletableFuture<Void> executeOrdersCreation(
            final String taskId,
            final List<OrderRequest> requests,
            final boolean transactional) {
        AsyncTask task = asyncTaskStorage.getTask(taskId);

        if (task == null) {
            return CompletableFuture.completedFuture(null);
        }

        task.setStatus(AsyncTaskStatus.IN_PROGRESS);

        try {
            delay(startupDelayMillis);
            if (transactional) {
                executeTransactionalImport(task, requests);
            } else {
                executeNonTransactionalImport(task, requests);
            }

            task.setStatus(AsyncTaskStatus.COMPLETED);
            task.setEndTime(LocalDateTime.now());
            task.setProgress(100);
            task.setResult("Created " + requests.size() + " orders");
        } catch (AsyncProcessingInterruptedException exception) {
            markTaskAsFailed(task, "Task was interrupted while waiting");
        } catch (Exception exception) {
            markTaskAsFailed(task, "Error: " + exception.getMessage());
        }

        return CompletableFuture.completedFuture(null);
    }

    private void executeTransactionalImport(final AsyncTask task, final List<OrderRequest> requests) {
        TransactionTemplate transactionTemplate = new TransactionTemplate(transactionManager);
        transactionTemplate.executeWithoutResult(status -> processRequests(task, requests));
    }

    private void executeNonTransactionalImport(final AsyncTask task, final List<OrderRequest> requests) {
        processRequests(task, requests);
    }

    private void processRequests(final AsyncTask task, final List<OrderRequest> requests) {
        int total = requests.size();
        for (int index = 0; index < total; index++) {
            orderService.createOrder(requests.get(index));
            task.setProgress((index + 1) * 100 / total);
            delay(perOrderDelayMillis);
        }
    }

    private void delay(final long delayMillis) {
        if (Thread.currentThread().isInterrupted()) {
            throw new AsyncProcessingInterruptedException();
        }

        if (delayMillis <= 0) {
            return;
        }

        try {
            Thread.sleep(delayMillis);
        } catch (InterruptedException exception) {
            Thread.currentThread().interrupt();
            throw new AsyncProcessingInterruptedException();
        }
    }

    private void markTaskAsFailed(final AsyncTask task, final String result) {
        task.setStatus(AsyncTaskStatus.FAILED);
        task.setEndTime(LocalDateTime.now());
        task.setResult(result);
    }
}
