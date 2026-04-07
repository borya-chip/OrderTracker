package com.order.tracker.controller;

import com.order.tracker.controller.api.OrderControllerApi;
import com.order.tracker.dto.request.OrderRequest;
import com.order.tracker.dto.request.OrderUpdateRequest;
import com.order.tracker.dto.response.OrderResponse;
import com.order.tracker.service.OrderService;
import jakarta.validation.Valid;
import java.time.LocalDate;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
public class OrderController implements OrderControllerApi {

    private final OrderService orderService;

    @GetMapping("/api/v1/orders/{id}")
    public ResponseEntity<OrderResponse> getById(@PathVariable final Long id) {
        return ResponseEntity.ok(orderService.getOrderById(id));
    }

    @GetMapping("/api/v1/orders")
    public ResponseEntity<List<OrderResponse>> getByDateRange(
            @RequestParam(required = false) final LocalDate startDate,
            @RequestParam(required = false) final LocalDate endDate,
            @RequestParam(required = false, defaultValue = "false") final boolean withEntityGraph) {
        if (startDate == null && endDate == null) {
            return ResponseEntity.ok(orderService.getAllOrders(withEntityGraph));
        }
        return ResponseEntity.ok(orderService.getOrdersByDateRange(startDate, endDate));
    }

    @PostMapping("/api/v1/orders")
    public ResponseEntity<OrderResponse> createOrder(@Valid @RequestBody final OrderRequest request) {
        OrderResponse response = orderService.createOrder(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @PostMapping("/api/v1/orders/bulk")
    public ResponseEntity<List<OrderResponse>> createOrdersBulk(
            @Valid @RequestBody final List<@Valid OrderRequest> requests,
            @RequestParam(defaultValue = "true") final boolean transactional) {
        List<OrderResponse> response = transactional
                ? orderService.createOrdersBulkTx(requests)
                : orderService.createOrdersBulkNoTx(requests);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @PatchMapping("/api/v1/orders/{id}")
    public ResponseEntity<OrderResponse> updateOrder(
            @PathVariable final Long id,
            @Valid @RequestBody final OrderUpdateRequest request) {
        return ResponseEntity.ok(orderService.updateOrder(id, request));
    }

    @DeleteMapping("/api/v1/orders/{id}")
    public ResponseEntity<Void> deleteOrder(@PathVariable final Long id) {
        orderService.deleteOrder(id);
        return ResponseEntity.noContent().build();
    }
}
