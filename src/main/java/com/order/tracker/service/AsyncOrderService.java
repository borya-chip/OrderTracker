package com.order.tracker.service;

import com.order.tracker.domain.AsyncTask;
import com.order.tracker.dto.request.OrderRequest;
import java.util.List;
import java.util.Map;

public interface AsyncOrderService {

    String createOrdersAsync(List<OrderRequest> requests, boolean transactional);

    AsyncTask getOrderTaskStatus(String taskId);

    Map<String, AsyncTask> getAllAsyncTasks();
}
