package com.order.tracker.service.impl;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertIterableEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

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
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.atomic.AtomicLong;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class OrderServiceImplTest {

    @Mock
    private OrderRepository orderRepository;

    @Mock
    private CustomerRepository customerRepository;

    @Mock
    private MealRepository mealRepository;

    private OrderServiceImpl service;

    @BeforeEach
    void setUp() {
        service = new OrderServiceImpl(
                orderRepository,
                customerRepository,
                mealRepository,
                new OrderMapper());
    }

    @Test
    void getOrderByIdShouldReturnMappedOrder() {
        Order order = order(5L, customer(1L), Set.of(meal(10L), meal(11L)));
        when(orderRepository.findByIdWithDetails(5L)).thenReturn(Optional.of(order));

        var response = service.getOrderById(5L);

        assertEquals(5L, response.getId());
        assertEquals(1L, response.getCustomerId());
        assertIterableEquals(Set.of(10L, 11L), response.getMealIds());
    }

    @Test
    void getOrderByIdShouldThrowWhenMissing() {
        when(orderRepository.findByIdWithDetails(5L)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> service.getOrderById(5L));
    }

    @Test
    void getAllOrdersShouldSupportBothRepositoryModes() {
        Order order = order(5L, customer(1L), Set.of(meal(10L)));
        when(orderRepository.findAllOrders()).thenReturn(List.of(order));
        when(orderRepository.findAllOrdersWithEntityGraph()).thenReturn(List.of(order));

        var plain = service.getAllOrders(false);
        var withEntityGraph = service.getAllOrders(true);

        assertEquals(1, plain.size());
        assertEquals(1, withEntityGraph.size());
    }

    @Test
    void getOrdersByDateRangeShouldReturnOrdersForWholeDayWindow() {
        Order order = order(5L, customer(1L), Set.of(meal(10L)));
        when(orderRepository.findByDateBetween(
                LocalDate.of(2026, 4, 1).atStartOfDay(),
                LocalDate.of(2026, 4, 5).atTime(23, 59, 59, 999_999_999)))
                .thenReturn(List.of(order));

        var response = service.getOrdersByDateRange(LocalDate.of(2026, 4, 1), LocalDate.of(2026, 4, 5));

        assertEquals(1, response.size());
    }

    @Test
    void getOrdersByDateRangeShouldRejectMissingDates() {
        LocalDate endDate = LocalDate.of(2026, 4, 1);

        assertThrows(
                BadRequestException.class,
                () -> service.getOrdersByDateRange(null, endDate));
    }

    @Test
    void getOrdersByDateRangeShouldRejectMissingEndDate() {
        LocalDate startDate = LocalDate.of(2026, 4, 1);

        assertThrows(
                BadRequestException.class,
                () -> service.getOrdersByDateRange(startDate, null));
    }

    @Test
    void getOrdersByDateRangeShouldRejectReversedDates() {
        LocalDate startDate = LocalDate.of(2026, 4, 5);
        LocalDate endDate = LocalDate.of(2026, 4, 1);

        assertThrows(
                BadRequestException.class,
                () -> service.getOrdersByDateRange(startDate, endDate));
    }

    @Test
    void createOrderShouldSaveOrderWithResolvedRelations() {
        Customer customer = customer(1L);
        Meal meal = meal(10L);
        OrderRequest request = request("Lunch", "18.50", 1L, Set.of(10L));

        when(customerRepository.findById(1L)).thenReturn(Optional.of(customer));
        when(mealRepository.findAllById(Set.of(10L))).thenReturn(List.of(meal));
        when(orderRepository.save(any(Order.class))).thenAnswer(invocation -> {
            Order order = invocation.getArgument(0);
            order.setId(7L);
            return order;
        });

        var response = service.createOrder(request);

        assertEquals(7L, response.getId());
        assertEquals("Lunch", response.getDescription());
        assertIterableEquals(Set.of(10L), response.getMealIds());
    }

    @Test
    void createOrderShouldThrowWhenSomeMealsAreMissing() {
        OrderRequest request = request("Lunch", "18.50", 1L, Set.of(10L, 11L));
        when(customerRepository.findById(1L)).thenReturn(Optional.of(customer(1L)));
        when(mealRepository.findAllById(Set.of(10L, 11L))).thenReturn(List.of(meal(10L)));

        assertThrows(
                ResourceNotFoundException.class,
                () -> service.createOrder(request));

        verify(orderRepository, never()).save(any(Order.class));
    }

    @Test
    void createOrdersBulkTxShouldCreateAllOrders() {
        Customer customer = customer(1L);
        Meal meal = meal(10L);
        OrderRequest first = request("Lunch", "18.50", 1L, Set.of(10L));
        OrderRequest second = request("Dinner", "25.00", 1L, Set.of(10L));

        when(customerRepository.findById(1L)).thenReturn(Optional.of(customer));
        when(mealRepository.findAllById(Set.of(10L))).thenReturn(List.of(meal));

        AtomicLong ids = new AtomicLong(20L);
        when(orderRepository.save(any(Order.class))).thenAnswer(invocation -> {
            Order order = invocation.getArgument(0);
            order.setId(ids.getAndIncrement());
            return order;
        });

        List<OrderResponse> response = service.createOrdersBulkTx(List.of(first, second));

        assertEquals(2, response.size());
        assertEquals(20L, response.get(0).getId());
        assertEquals(21L, response.get(1).getId());
        verify(orderRepository, times(2)).save(any(Order.class));
    }

    @Test
    void createOrdersBulkTxShouldRejectEmptyRequest() {
        List<OrderRequest> requests = List.of();

        BadRequestException exception = assertThrows(
                BadRequestException.class,
                () -> service.createOrdersBulkTx(requests));

        assertTrue(exception.getMessage().contains("at least one item"));
        verify(orderRepository, never()).save(any(Order.class));
    }

    @Test
    void createOrdersBulkNoTxShouldCreateAllOrders() {
        Customer customer = customer(1L);
        Meal meal = meal(10L);
        OrderRequest first = request("Lunch", "18.50", 1L, Set.of(10L));
        OrderRequest second = request("Dinner", "25.00", 1L, Set.of(10L));

        when(customerRepository.findById(1L)).thenReturn(Optional.of(customer));
        when(mealRepository.findAllById(Set.of(10L))).thenReturn(List.of(meal));

        AtomicLong ids = new AtomicLong(30L);
        when(orderRepository.save(any(Order.class))).thenAnswer(invocation -> {
            Order order = invocation.getArgument(0);
            order.setId(ids.getAndIncrement());
            return order;
        });

        var response = service.createOrdersBulkNoTx(List.of(first, second));

        assertEquals(2, response.size());
        assertEquals(30L, response.get(0).getId());
    }

    @Test
    void updateOrderShouldKeepCurrentValuesWhenOptionalFieldsAreMissing() {
        Order existing = order(5L, customer(1L), Set.of(meal(10L)));
        OrderUpdateRequest request = new OrderUpdateRequest();
        request.setAmount(new BigDecimal("30.00"));

        when(orderRepository.findById(5L)).thenReturn(Optional.of(existing));
        when(orderRepository.save(any(Order.class))).thenAnswer(invocation -> invocation.getArgument(0));

        var response = service.updateOrder(5L, request);

        assertEquals(0, new BigDecimal("30.00").compareTo(response.getAmount()));
        assertEquals("Order 5", response.getDescription());
        assertEquals(1L, response.getCustomerId());
        assertIterableEquals(Set.of(10L), response.getMealIds());
    }

    @Test
    void updateOrderShouldChangeAllProvidedFields() {
        Order existing = order(5L, customer(1L), Set.of(meal(10L)));
        Customer newCustomer = customer(2L);
        Meal newMeal = meal(20L);
        OrderUpdateRequest request = new OrderUpdateRequest();
        request.setAmount(new BigDecimal("30.00"));
        request.setDate(LocalDateTime.of(2026, 4, 6, 15, 30));
        request.setDescription("Updated");
        request.setCustomerId(2L);
        request.setMealIds(Set.of(20L));

        when(orderRepository.findById(5L)).thenReturn(Optional.of(existing));
        when(customerRepository.findById(2L)).thenReturn(Optional.of(newCustomer));
        when(mealRepository.findAllById(Set.of(20L))).thenReturn(List.of(newMeal));
        when(orderRepository.save(any(Order.class))).thenAnswer(invocation -> invocation.getArgument(0));

        var response = service.updateOrder(5L, request);

        assertEquals(0, new BigDecimal("30.00").compareTo(response.getAmount()));
        assertEquals("Updated", response.getDescription());
        assertEquals(2L, response.getCustomerId());
        assertIterableEquals(Set.of(20L), response.getMealIds());
    }

    @Test
    void updateOrderShouldThrowWhenOrderIsMissing() {
        OrderUpdateRequest request = new OrderUpdateRequest();
        when(orderRepository.findById(5L)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> service.updateOrder(5L, request));
    }

    @Test
    void deleteOrderShouldDeleteExistingOrder() {
        when(orderRepository.existsById(5L)).thenReturn(true);

        service.deleteOrder(5L);

        verify(orderRepository).deleteById(5L);
    }

    @Test
    void deleteOrderShouldThrowWhenMissing() {
        when(orderRepository.existsById(5L)).thenReturn(false);

        assertThrows(ResourceNotFoundException.class, () -> service.deleteOrder(5L));
    }

    private static OrderRequest request(
            final String description,
            final String amount,
            final Long customerId,
            final Set<Long> mealIds) {
        return new OrderRequest(
                new BigDecimal(amount),
                LocalDateTime.of(2026, 4, 5, 12, 0),
                description,
                customerId,
                mealIds);
    }

    private static Customer customer(final Long id) {
        Customer customer = new Customer();
        customer.setId(id);
        customer.setFirstName("Alex");
        customer.setLastName("Brown");
        customer.setEmail("alex@example.com");
        return customer;
    }

    private static Meal meal(final Long id) {
        Meal meal = new Meal();
        meal.setId(id);
        meal.setName("Meal " + id);
        meal.setPrice(new BigDecimal("10.00"));
        meal.setCookingTime(15);
        return meal;
    }

    private static Order order(final Long id, final Customer customer, final Set<Meal> meals) {
        Order order = new Order();
        order.setId(id);
        order.setAmount(new BigDecimal("25.00"));
        order.setDate(LocalDateTime.of(2026, 4, 5, 12, 0));
        order.setDescription("Order " + id);
        order.setCustomer(customer);
        order.setMeals(new LinkedHashSet<>(meals));
        return order;
    }
}
