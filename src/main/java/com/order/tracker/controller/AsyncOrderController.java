package com.order.tracker.controller;

import com.order.tracker.controller.api.AsyncOrderControllerApi;
import com.order.tracker.domain.AsyncTask;
import com.order.tracker.dto.request.OrderRequest;
import com.order.tracker.service.AsyncOrderService;
import jakarta.validation.Valid;
import java.util.List;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/orders/async")
@RequiredArgsConstructor
public class AsyncOrderController implements AsyncOrderControllerApi {

    private final AsyncOrderService asyncOrderService;

    @PostMapping
    public ResponseEntity<Map<String, String>> createOrdersAsync(
            @Valid @RequestBody final List<@Valid OrderRequest> requests,
            @RequestParam(defaultValue = "true") final boolean transactional) {
        String taskId = asyncOrderService.createOrdersAsync(requests, transactional);
        return ResponseEntity.accepted().body(Map.of("taskId", taskId));
    }

    @GetMapping("/status/{taskId}")
    public ResponseEntity<AsyncTask> getTaskStatus(@PathVariable final String taskId) {
        AsyncTask task = asyncOrderService.getOrderTaskStatus(taskId);
        if (task == null) {
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.ok(task);
    }

    @GetMapping("/tasks")
    public ResponseEntity<Map<String, AsyncTask>> getAllAsyncTasks() {
        return ResponseEntity.ok(asyncOrderService.getAllAsyncTasks());
    }
}
