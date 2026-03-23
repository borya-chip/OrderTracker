package com.order.tracker.service.impl;

import com.order.tracker.domain.Category;
import com.order.tracker.domain.Customer;
import com.order.tracker.domain.Meal;
import com.order.tracker.domain.Order;
import com.order.tracker.domain.Restaurant;
import com.order.tracker.dto.request.OrderRequest;
import com.order.tracker.dto.request.OrderTransactionRequest;
import com.order.tracker.dto.response.OrderResponse;
import com.order.tracker.exception.BadRequestException;
import com.order.tracker.exception.ResourceNotFoundException;
import com.order.tracker.mapper.OrderMapper;
import com.order.tracker.repository.CategoryRepository;
import com.order.tracker.repository.CustomerRepository;
import com.order.tracker.repository.MealRepository;
import com.order.tracker.repository.OrderRepository;
import com.order.tracker.repository.RestaurantRepository;
import com.order.tracker.service.OrderService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;

@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
public class OrderServiceImpl implements OrderService {

    private static final String DEFAULT_TRANSACTION_DESCRIPTION = "Transaction order";

    private final OrderRepository orderRepository;
    private final CustomerRepository customerRepository;
    private final CategoryRepository categoryRepository;
    private final RestaurantRepository restaurantRepository;
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
                .orElseThrow(() -> new ResourceNotFoundException("Order not found: " + id));
        return orderMapper.toResponse(order);
    }

    @Override
    @Transactional(readOnly = true)
    public List<OrderResponse> getByDateRange(
            final LocalDate startDate,
            final LocalDate endDate,
            final boolean optimizedFetch) {

        if (startDate != null && endDate != null && startDate.isAfter(endDate)) {
            throw new BadRequestException("startDate must be <= endDate");
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
                .orElseThrow(() -> new ResourceNotFoundException("Order not found: " + id));
        apply(order, request);
        return orderMapper.toResponse(orderRepository.save(order));
    }

    @Override
    @Transactional
    public void delete(final Long id) {
        if (!orderRepository.existsById(id)) {
            throw new ResourceNotFoundException("Order not found: " + id);
        }
        orderRepository.deleteById(id);
    }

    @Override
    @Transactional
    public void createOrderTransactionTx(final OrderTransactionRequest request, final boolean failAfterMealsSave) {
        executeOrderTransaction(request, failAfterMealsSave);
    }

    @Override
    @Transactional(propagation = Propagation.NOT_SUPPORTED)
    public void createOrderTransactionNoTx(final OrderTransactionRequest request, final boolean failAfterMealsSave) {
        executeOrderTransaction(request, failAfterMealsSave);
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
                .orElseThrow(() -> new ResourceNotFoundException("Customer not found: " + customerId));
    }

    private Set<Meal> findMeals(final Set<Long> mealIds) {
        List<Meal> meals = mealRepository.findAllById(mealIds);
        if (meals.size() != mealIds.size()) {
            throw new ResourceNotFoundException("One or more meals were not found");
        }
        return meals.stream().collect(java.util.stream.Collectors.toCollection(LinkedHashSet::new));
    }

    private LocalDateTime toStartOfDay(final LocalDate date) {
        return date.atStartOfDay();
    }

    private LocalDateTime toEndOfDay(final LocalDate date) {
        return date.atTime(LocalTime.MAX);
    }

    private void executeOrderTransaction(final OrderTransactionRequest request, final boolean failAfterMealsSave) {
        String suffix = UUID.randomUUID().toString().replace("-", "").substring(0, 8);
        validatePositiveAmount(request.getAmount());

        Customer customer = getCustomer(request.getCustomerId());
        Category category = getCategory(request.getCategoryId());
        Restaurant restaurant = getRestaurant(request.getRestaurantId());
        LocalDateTime orderedAt = request.getOrderedAt() != null ? request.getOrderedAt() : LocalDateTime.now();
        String description = normalizeTransactionDescription(request.getDescription());

        Meal mealOne = mealRepository.save(new Meal(
                null,
                "Tx Meal A " + suffix,
                new BigDecimal("10.50"),
                15,
                category,
                restaurant,
                new LinkedHashSet<>()));

        Meal mealTwo = mealRepository.save(new Meal(
                null,
                "Tx Meal B " + suffix,
                new BigDecimal("8.40"),
                12,
                category,
                restaurant,
                new LinkedHashSet<>()));

        if (failAfterMealsSave) {
            throw new IllegalStateException("Forced error after meals save");
        }

        Order order = new Order();
        order.setAmount(request.getAmount());
        order.setDate(orderedAt);
        order.setDescription(description);
        order.setCustomer(customer);
        order.setMeals(new LinkedHashSet<>(Set.of(mealOne, mealTwo)));
        orderRepository.save(order);
    }

    private void validatePositiveAmount(final BigDecimal amount) {
        if (amount == null || amount.compareTo(BigDecimal.ZERO) <= 0) {
            throw new BadRequestException("Order amount must be > 0");
        }
    }

    private Customer getCustomer(final Long customerId) {
        return customerRepository.findById(customerId)
                .orElseThrow(() -> new ResourceNotFoundException("Customer not found: " + customerId));
    }

    private Category getCategory(final Long categoryId) {
        return categoryRepository.findById(categoryId)
                .orElseThrow(() -> new ResourceNotFoundException("Category not found: " + categoryId));
    }

    private Restaurant getRestaurant(final Long restaurantId) {
        return restaurantRepository.findById(restaurantId)
                .orElseThrow(() -> new ResourceNotFoundException("Restaurant not found: " + restaurantId));
    }

    private String normalizeTransactionDescription(final String description) {
        if (description == null || description.isBlank()) {
            return DEFAULT_TRANSACTION_DESCRIPTION;
        }
        String normalized = description.trim();
        if (normalized.length() > 255) {
            throw new BadRequestException("Description length must be <= 255");
        }
        return normalized;
    }
}
