package com.order.tracker.service.impl;

import com.order.tracker.domain.Customer;
import com.order.tracker.domain.Meal;
import com.order.tracker.domain.Order;
import com.order.tracker.dto.request.OrderRequest;
import com.order.tracker.dto.response.OrderResponse;
import com.order.tracker.mapper.OrderMapper;
import com.order.tracker.repository.CustomerRepository;
import com.order.tracker.repository.MealRepository;
import com.order.tracker.repository.OrderRepository;
import com.order.tracker.service.OrderService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
public class OrderServiceImpl implements OrderService {

    private final OrderRepository orderRepository;
    private final CustomerRepository customerRepository;
    private final MealRepository mealRepository;
    private final OrderMapper orderMapper;

    @Override
    @Transactional
    public OrderResponse create(final OrderRequest request) {
        Order order = new Order();
        apply(order, request);
        return orderMapper.toResponse(orderRepository.save(order));
    }

    @Override
    @Transactional(readOnly = true)
    public OrderResponse getById(final Long id) {
        Order order = orderRepository.findByIdWithDetails(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Order not found: " + id));
        return orderMapper.toResponse(order);
    }

    @Override
    @Transactional(readOnly = true)
    public List<OrderResponse> getByDateRange(
            final LocalDate startDate,
            final LocalDate endDate,
            final boolean optimizedFetch) {

        if (startDate != null && endDate != null && startDate.isAfter(endDate)) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "startDate must be <= endDate");
        }

        List<Order> orders;

        if (startDate == null && endDate == null) {
            orders = optimizedFetch ? orderRepository.findAllWithDetails() : orderRepository.findAll();
        } else if (startDate == null) {
            LocalDateTime end = toEndOfDay(endDate);
            orders = optimizedFetch
                    ? orderRepository.findWithDetailsByDateLessThanEqual(end)
                    : orderRepository.findByDateLessThanEqual(end);
        } else if (endDate == null) {
            LocalDateTime start = toStartOfDay(startDate);
            orders = optimizedFetch
                    ? orderRepository.findWithDetailsByDateGreaterThanEqual(start)
                    : orderRepository.findByDateGreaterThanEqual(start);
        } else {
            LocalDateTime start = toStartOfDay(startDate);
            LocalDateTime end = toEndOfDay(endDate);
            orders = optimizedFetch
                    ? orderRepository.findWithDetailsByDateBetween(start, end)
                    : orderRepository.findByDateBetween(start, end);
        }

        return orders.stream()
                .map(orderMapper::toResponse)
                .toList();
    }

    @Override
    @Transactional
    public OrderResponse update(final Long id, final OrderRequest request) {
        Order order = orderRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Order not found: " + id));
        apply(order, request);
        return orderMapper.toResponse(orderRepository.save(order));
    }

    @Override
    @Transactional
    public void delete(final Long id) {
        if (!orderRepository.existsById(id)) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Order not found: " + id);
        }
        orderRepository.deleteById(id);
    }

    private void apply(final Order order, final OrderRequest request) {
        order.setAmount(request.getAmount());
        order.setDate(request.getDate());
        order.setDescription(request.getDescription());
        order.setCustomer(findCustomer(request.getCustomerId()));
        order.setMeals(findMeals(request.getMealIds()));
    }

    private Customer findCustomer(final Long customerId) {
        return customerRepository.findById(customerId)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND,
                        "Customer not found: " + customerId));
    }

    private Set<Meal> findMeals(final Set<Long> mealIds) {
        List<Meal> meals = mealRepository.findAllById(mealIds);
        if (meals.size() != mealIds.size()) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "One or more meals were not found");
        }
        return meals.stream().collect(java.util.stream.Collectors.toCollection(LinkedHashSet::new));
    }

    private LocalDateTime toStartOfDay(final LocalDate date) {
        return date.atStartOfDay();
    }

    private LocalDateTime toEndOfDay(final LocalDate date) {
        return date.atTime(LocalTime.MAX);
    }
}
