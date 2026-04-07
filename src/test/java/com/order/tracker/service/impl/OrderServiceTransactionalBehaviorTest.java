package com.order.tracker.service.impl;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import com.order.tracker.domain.Category;
import com.order.tracker.domain.Customer;
import com.order.tracker.domain.Meal;
import com.order.tracker.domain.Restaurant;
import com.order.tracker.dto.request.OrderRequest;
import com.order.tracker.exception.ResourceNotFoundException;
import com.order.tracker.repository.CategoryRepository;
import com.order.tracker.repository.CustomerRepository;
import com.order.tracker.repository.MealRepository;
import com.order.tracker.repository.OrderRepository;
import com.order.tracker.repository.RestaurantRepository;
import com.order.tracker.service.OrderService;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Set;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

@SpringBootTest(properties = "LOG_DIR=/tmp")
@ActiveProfiles("test")
class OrderServiceTransactionalBehaviorTest {

    @Autowired
    private OrderService orderService;

    @Autowired
    private OrderRepository orderRepository;

    @Autowired
    private MealRepository mealRepository;

    @Autowired
    private CustomerRepository customerRepository;

    @Autowired
    private RestaurantRepository restaurantRepository;

    @Autowired
    private CategoryRepository categoryRepository;

    @BeforeEach
    void setUp() {
        orderRepository.deleteAll();
        mealRepository.deleteAll();
        customerRepository.deleteAll();
        restaurantRepository.deleteAll();
        categoryRepository.deleteAll();
    }

    @Test
    void createOrdersBulkTxShouldRollbackWholeBatchWhenOneItemFails() {
        TestData testData = seedData();
        List<OrderRequest> requests = List.of(validRequest(testData), invalidRequest(testData));

        assertThrows(ResourceNotFoundException.class, () -> orderService.createOrdersBulkTx(requests));

        assertEquals(0, orderRepository.count());
    }

    @Test
    void createOrdersBulkNoTxShouldKeepPreviouslySavedOrdersWhenOneItemFails() {
        TestData testData = seedData();
        List<OrderRequest> requests = List.of(validRequest(testData), invalidRequest(testData));

        assertThrows(ResourceNotFoundException.class, () -> orderService.createOrdersBulkNoTx(requests));

        assertEquals(1, orderRepository.count());
    }

    private TestData seedData() {
        Category category = new Category();
        category.setName("Pizza");
        category = categoryRepository.save(category);

        Restaurant restaurant = new Restaurant();
        restaurant.setName("Roma");
        restaurant.setContactEmail("roma@example.com");
        restaurant.setCity("Minsk");
        restaurant.setAddress("Main 1");
        restaurant.setPhone("+123");
        restaurant.setActive(true);
        restaurant = restaurantRepository.save(restaurant);

        Customer customer = new Customer();
        customer.setFirstName("Alex");
        customer.setLastName("Brown");
        customer.setEmail("alex@example.com");
        customer.setPhoneNumber("+375");
        customer = customerRepository.save(customer);

        Meal meal = new Meal();
        meal.setName("Margherita");
        meal.setPrice(new BigDecimal("12.50"));
        meal.setCookingTime(15);
        meal.setCategory(category);
        meal.setRestaurant(restaurant);
        meal = mealRepository.save(meal);

        return new TestData(customer.getId(), meal.getId());
    }

    private OrderRequest validRequest(final TestData testData) {
        return new OrderRequest(
                new BigDecimal("25.00"),
                LocalDateTime.of(2026, 4, 5, 13, 0),
                "First order",
                testData.customerId(),
                Set.of(testData.mealId()));
    }

    private OrderRequest invalidRequest(final TestData testData) {
        return new OrderRequest(
                new BigDecimal("30.00"),
                LocalDateTime.of(2026, 4, 5, 14, 0),
                "Broken order",
                testData.customerId(),
                Set.of(testData.mealId(), 999999L));
    }

    private record TestData(Long customerId, Long mealId) {
    }
}
