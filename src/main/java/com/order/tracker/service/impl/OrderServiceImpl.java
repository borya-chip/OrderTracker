package com.order.tracker.service.impl;

import com.order.tracker.domain.Customer;
import com.order.tracker.domain.Meal;
import com.order.tracker.domain.Order;
import com.order.tracker.dto.request.OrderRequest;
import com.order.tracker.dto.request.OrderUpdateRequest;
import com.order.tracker.dto.response.OrderResponse;
import com.order.tracker.exception.BadRequestException;
import com.order.tracker.exception.ResourceNotFoundException;
import com.order.tracker.mapper.OrderMapper;
import com.order.tracker.repository.CustomerRepository;
import com.order.tracker.repository.MealRepository;
import com.order.tracker.repository.OrderRepository;
import com.order.tracker.service.OrderService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;

@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
public class OrderServiceImpl implements OrderService {

    private static final String ORDER_NOT_FOUND_MESSAGE = "Order not found: ";
    private static final String BULK_REQUEST_EMPTY_MESSAGE =
            "Bulk order request must contain at least one item";

    private final OrderRepository orderRepository;
    private final CustomerRepository customerRepository;
    private final MealRepository mealRepository;
    private final OrderMapper orderMapper;

    @Override
    @Transactional
    public OrderResponse createOrder(final OrderRequest request) {
        return orderMapper.toResponse(createOrderEntity(request));
    }

    @Override
    @Transactional
    public List<OrderResponse> createOrdersBulkTx(final List<OrderRequest> requests) {
        return createOrdersBulk(requests);
    }

    @Override
    @Transactional(propagation = Propagation.NOT_SUPPORTED)
    public List<OrderResponse> createOrdersBulkNoTx(final List<OrderRequest> requests) {
        return createOrdersBulk(requests);
    }

    @Override
    @Transactional(readOnly = true)
    public OrderResponse getOrderById(final Long id) {
        Order order = orderRepository.findByIdWithDetails(id)
                .orElseThrow(() -> new ResourceNotFoundException(ORDER_NOT_FOUND_MESSAGE + id));
        return orderMapper.toResponse(order);
    }

    @Override
    @Transactional(readOnly = true)
    public List<OrderResponse> getAllOrders(final boolean withEntityGraph) {
        List<Order> orders;
        if (withEntityGraph) {
            orders = orderRepository.findAllOrdersWithEntityGraph();
        } else {
            orders = orderRepository.findAllOrders();
        }
        return toResponses(orders);
    }

    @Override
    @Transactional(readOnly = true)
    public List<OrderResponse> getOrdersByDateRange(final LocalDate startDate, final LocalDate endDate) {
        if (startDate == null || endDate == null) {
            throw new BadRequestException("Both startDate and endDate are required for date range filtering");
        }
        if (startDate.isAfter(endDate)) {
            throw new BadRequestException("startDate must be <= endDate");
        }

        LocalDateTime startDateTime = startDate.atStartOfDay();
        LocalDateTime endDateTime = endDate.atTime(23, 59, 59, 999_999_999);
        List<Order> orders = orderRepository.findByDateBetween(startDateTime, endDateTime);
        return toResponses(orders);
    }

    @Override
    @Transactional
    public OrderResponse updateOrder(final Long id, final OrderUpdateRequest request) {
        Order order = orderRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException(ORDER_NOT_FOUND_MESSAGE + id));

        BigDecimal amount = Optional.ofNullable(request.getAmount())
                .orElse(order.getAmount());
        LocalDateTime date = Optional.ofNullable(request.getDate())
                .orElse(order.getDate());
        String description = Optional.ofNullable(request.getDescription())
                .orElse(order.getDescription());
        Customer customer = Optional.ofNullable(request.getCustomerId())
                .map(this::findCustomer)
                .orElse(order.getCustomer());
        Set<Meal> meals = Optional.ofNullable(request.getMealIds())
                .map(this::findMeals)
                .orElse(order.getMeals());

        order.setAmount(amount);
        order.setDate(date);
        order.setDescription(description);
        order.setCustomer(customer);
        order.setMeals(meals);
        return orderMapper.toResponse(orderRepository.save(order));
    }

    @Override
    @Transactional
    public void deleteOrder(final Long id) {
        if (!orderRepository.existsById(id)) {
            throw new ResourceNotFoundException(ORDER_NOT_FOUND_MESSAGE + id);
        }
        orderRepository.deleteById(id);
    }

    private Customer findCustomer(final Long customerId) {
        return customerRepository.findById(customerId)
                .orElseThrow(() -> new ResourceNotFoundException("Customer not found: " + customerId));
    }

    private Set<Meal> findMeals(final Set<Long> mealIds) {
        return Optional.ofNullable(mealIds)
                .filter(ids -> !ids.isEmpty())
                .map(ids -> {
                    List<Meal> meals = mealRepository.findAllById(ids);
                    if (meals.size() != ids.size()) {
                        throw new ResourceNotFoundException("One or more meals were not found");
                    }
                    return meals.stream()
                            .collect(java.util.stream.Collectors.toCollection(LinkedHashSet::new));
                })
                .orElseThrow(() -> new BadRequestException("Order must contain at least one meal"));
    }

    private Order createOrderEntity(final OrderRequest request) {
        Customer customer = findCustomer(request.getCustomerId());
        Set<Meal> meals = findMeals(request.getMealIds());
        Order order = orderMapper.fromRequest(request, customer, meals);
        return orderRepository.save(order);
    }

    private List<OrderResponse> createOrdersBulk(final List<OrderRequest> requests) {
        List<OrderRequest> bulkRequests = Optional.ofNullable(requests)
                .filter(items -> !items.isEmpty())
                .orElseThrow(() -> new BadRequestException(BULK_REQUEST_EMPTY_MESSAGE));

        return bulkRequests.stream()
                .map(this::createOrderEntity)
                .map(orderMapper::toResponse)
                .toList();
    }

    private List<OrderResponse> toResponses(final List<Order> orders) {
        return orders.stream()
                .map(orderMapper::toResponse)
                .toList();
    }
}
