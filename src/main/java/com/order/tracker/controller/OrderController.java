package com.order.tracker.controller;

import com.order.tracker.controller.api.OrderControllerApi;
import com.order.tracker.dto.request.OrderRequest;
import com.order.tracker.dto.request.OrderTransactionRequest;
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
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
public class OrderController implements OrderControllerApi {

    private final OrderService orderService;

    @PostMapping("/api/v1/orders")
    public ResponseEntity<OrderResponse> create(@Valid @RequestBody final OrderRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(orderService.create(request));
    }

    @GetMapping("/api/v1/orders/{id}")
    public ResponseEntity<OrderResponse> getById(@PathVariable final Long id) {
        return ResponseEntity.ok(orderService.getById(id));
    }

    @GetMapping("/api/v1/orders")
    public ResponseEntity<List<OrderResponse>> getByDateRange(
            @RequestParam(required = false) final LocalDate startDate,
            @RequestParam(required = false) final LocalDate endDate,
            @RequestParam(defaultValue = "false") final boolean optimizedFetch) {
        return ResponseEntity.ok(orderService.getByDateRange(startDate, endDate, optimizedFetch));
    }

    @PostMapping("/api/v1/orders/transaction")
    public ResponseEntity<Void> createOrderTransaction(
            @Valid @RequestBody final OrderTransactionRequest request,
            @RequestParam(defaultValue = "true") final boolean transactional,
            @RequestParam(defaultValue = "false") final boolean failAfterMealsSave) {
        if (transactional) {
            orderService.createOrderTransactionTx(request, failAfterMealsSave);
        } else {
            orderService.createOrderTransactionNoTx(request, failAfterMealsSave);
        }
        return ResponseEntity.noContent().build();
    }

    @PutMapping("/api/v1/orders/{id}")
    public ResponseEntity<OrderResponse> update(
            @PathVariable final Long id,
            @Valid @RequestBody final OrderRequest request) {
        return ResponseEntity.ok(orderService.update(id, request));
    }

    @DeleteMapping("/api/v1/orders/{id}")
    public ResponseEntity<Void> delete(@PathVariable final Long id) {
        orderService.delete(id);
        return ResponseEntity.noContent().build();
    }
}
