package com.order.tracker.service;

import com.order.tracker.domain.Category;
import com.order.tracker.domain.Customer;
import com.order.tracker.domain.Meal;
import com.order.tracker.domain.Restaurant;
import com.order.tracker.dto.request.OrderRequest;
import com.order.tracker.dto.response.NPlusOneDemoResponse;
import com.order.tracker.repository.CategoryRepository;
import com.order.tracker.repository.CustomerRepository;
import com.order.tracker.repository.MealRepository;
import com.order.tracker.repository.OrderRepository;
import com.order.tracker.repository.RestaurantRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.LinkedHashSet;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertTrue;

@SpringBootTest
class NPlusOneDemoServiceIntegrationTest {

    @Autowired
    private NPlusOneDemoService nPlusOneDemoService;

    @Autowired
    private CustomerRepository customerRepository;

    @Autowired
    private CategoryRepository categoryRepository;

    @Autowired
    private RestaurantRepository restaurantRepository;

    @Autowired
    private MealRepository mealRepository;

    @Autowired
    private OrderRepository orderRepository;

    @Autowired
    private OrderService orderService;

    @BeforeEach
    void setUpData() {
        if (orderRepository.count() > 0) {
            return;
        }

        Customer customer = customerRepository.save(new Customer(
                null,
                "NPlusOne Customer",
                "nplusone@example.com",
                "+375291111111",
                new java.util.ArrayList<>()));

        Category category = categoryRepository.save(new Category(
                null,
                "NPlusOne Category",
                new java.util.ArrayList<>()));

        Restaurant restaurant = restaurantRepository.save(new Restaurant(
                null,
                "NPlusOne Restaurant",
                "nplusone-restaurant@example.com",
                "Minsk",
                "Pushkina 10",
                "+375171234567",
                true,
                new java.util.ArrayList<>()));

        Meal mealOne = mealRepository.save(new Meal(
                null,
                "NPlusOne Meal A",
                new BigDecimal("9.90"),
                15,
                category,
                restaurant,
                new LinkedHashSet<>()));

        Meal mealTwo = mealRepository.save(new Meal(
                null,
                "NPlusOne Meal B",
                new BigDecimal("12.30"),
                18,
                category,
                restaurant,
                new LinkedHashSet<>()));

        createOrder(
                customer.getId(),
                mealOne.getId(),
                mealTwo.getId(),
                new BigDecimal("22.20"),
                "Order 1",
                LocalDateTime.now().minusDays(2));
        createOrder(
                customer.getId(),
                mealOne.getId(),
                mealTwo.getId(),
                new BigDecimal("24.10"),
                "Order 2",
                LocalDateTime.now().minusDays(1));
        createOrder(
                customer.getId(),
                mealOne.getId(),
                mealTwo.getId(),
                new BigDecimal("19.40"),
                "Order 3",
                LocalDateTime.now());
    }

    @Test
    void shouldLoadOrdersWithFewerQueriesUsingEntityGraph() {
        NPlusOneDemoResponse response = nPlusOneDemoService.demonstrate();

        assertTrue(response.getOrdersCount() > 0);
        assertTrue(response.getQueriesWithNPlusOne() > response.getQueriesWithEntityGraph());
        assertTrue(response.getSavedQueries() > 0);
    }

    private void createOrder(
            final Long customerId,
            final Long mealOneId,
            final Long mealTwoId,
            final BigDecimal amount,
            final String description,
            final LocalDateTime date) {

        Set<Long> mealIds = new LinkedHashSet<>();
        mealIds.add(mealOneId);
        mealIds.add(mealTwoId);

        orderService.create(new OrderRequest(
                amount,
                date,
                description,
                customerId,
                mealIds));
    }
}
