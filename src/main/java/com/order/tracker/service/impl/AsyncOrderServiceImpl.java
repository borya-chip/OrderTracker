package com.order.tracker.service.impl;

import com.order.tracker.domain.AsyncTask;
import com.order.tracker.domain.AsyncTaskStatus;
import com.order.tracker.dto.request.OrderRequest;
import com.order.tracker.exception.BadRequestException;
import com.order.tracker.service.AsyncOrderService;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class AsyncOrderServiceImpl implements AsyncOrderService {

    private static final String BULK_REQUEST_EMPTY_MESSAGE =
            "Bulk order request must contain at least one item";

    private final AsyncTaskStorage asyncTaskStorage;
    private final AsyncOrderExecutorService asyncOrderExecutor;

    @Override
    public String createOrdersAsync(final List<OrderRequest> requests, final boolean transactional) {
        List<OrderRequest> bulkRequests = Optional.ofNullable(requests)
                .filter(items -> !items.isEmpty())
                .orElseThrow(() -> new BadRequestException(BULK_REQUEST_EMPTY_MESSAGE));

        String taskId = UUID.randomUUID().toString();

        AsyncTask task = AsyncTask.builder()
                .taskId(taskId)
                .status(AsyncTaskStatus.PENDING)
                .startTime(LocalDateTime.now())
                .progress(0)
                .build();

        asyncTaskStorage.saveTask(task);
        asyncOrderExecutor.executeOrdersCreation(taskId, bulkRequests, transactional);

        return taskId;
    }

    @Override
    public AsyncTask getOrderTaskStatus(final String taskId) {
        return asyncTaskStorage.getTask(taskId);
    }

    @Override
    public Map<String, AsyncTask> getAllAsyncTasks() {
        return asyncTaskStorage.getAllTasks();
    }
}
