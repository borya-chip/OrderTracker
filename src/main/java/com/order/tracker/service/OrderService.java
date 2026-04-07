package com.order.tracker.service;

import com.order.tracker.dto.request.OrderRequest;
import com.order.tracker.dto.request.OrderUpdateRequest;
import com.order.tracker.dto.response.OrderResponse;
import java.time.LocalDate;
import java.util.List;

public interface OrderService {

    OrderResponse getOrderById(Long id);

    List<OrderResponse> getAllOrders(boolean withEntityGraph);

    List<OrderResponse> getOrdersByDateRange(LocalDate startDate, LocalDate endDate);

    OrderResponse createOrder(OrderRequest request);

    List<OrderResponse> createOrdersBulkTx(List<OrderRequest> requests);

    List<OrderResponse> createOrdersBulkNoTx(List<OrderRequest> requests);

    OrderResponse updateOrder(Long id, OrderUpdateRequest request);

    void deleteOrder(Long id);
}
