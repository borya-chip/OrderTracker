package com.order.tracker.service;

import com.order.tracker.dto.request.OrderRequest;
import com.order.tracker.dto.request.OrderTransactionRequest;
import com.order.tracker.dto.response.OrderResponse;

import java.time.LocalDate;
import java.util.List;

public interface OrderService {

    OrderResponse create(OrderRequest request);

    OrderResponse getById(Long id);

    List<OrderResponse> getByDateRange(LocalDate startDate, LocalDate endDate, boolean optimizedFetch);

    OrderResponse update(Long id, OrderRequest request);

    void delete(Long id);

    void createOrderTransactionTx(OrderTransactionRequest request, boolean failAfterMealsSave);

    void createOrderTransactionNoTx(OrderTransactionRequest request, boolean failAfterMealsSave);
}
