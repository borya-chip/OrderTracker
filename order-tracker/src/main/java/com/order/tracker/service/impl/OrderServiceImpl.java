package com.order.tracker.service.impl;

import com.order.tracker.domain.Order;
import com.order.tracker.dto.OrderDto;
import com.order.tracker.mapper.OrderMapper;
import com.order.tracker.repository.OrderRepository;
import com.order.tracker.service.OrderService;

import java.time.LocalDate;
import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class OrderServiceImpl implements OrderService {

    private final OrderRepository repository;
    private final OrderMapper mapper;

    @Override
    public OrderDto getById(final Long id) {
        Order order = repository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Order not found: " + id));
        return mapper.toDto(order);
    }

    @Override
    public List<OrderDto> getByDateRange(final LocalDate startDate, final LocalDate endDate) {
        List<Order> orders;

        if (startDate == null && endDate == null) {
            orders = repository.findAll();
        } else if (startDate == null) {
            orders = repository.findByDateLessThanEqual(endDate);
        } else if (endDate == null) {
            orders = repository.findByDateGreaterThanEqual(startDate);
        } else {
            orders = repository.findByDateBetween(startDate, endDate);
        }

        return orders.stream()
                .map(mapper::toDto)
                .toList();
    }
}
