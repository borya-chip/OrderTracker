package com.order.tracker.service.impl;

import com.order.tracker.domain.Category;
import com.order.tracker.domain.Customer;
import com.order.tracker.domain.Meal;
import com.order.tracker.domain.Restaurant;
import com.order.tracker.dto.request.OrderRequest;
import com.order.tracker.repository.CategoryRepository;
import com.order.tracker.repository.CustomerRepository;
import com.order.tracker.repository.MealRepository;
import com.order.tracker.repository.RestaurantRepository;
import com.order.tracker.service.OrderService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.Set;

@Service
@RequiredArgsConstructor
public class TransactionDemoDataWriter {

    private final CustomerRepository customerRepository;
    private final CategoryRepository categoryRepository;
    private final RestaurantRepository restaurantRepository;
    private final MealRepository mealRepository;
    private final OrderService orderService;

    @Transactional(propagation = Propagation.NOT_SUPPORTED)
    public void writeAndFailWithoutTransaction(final String suffix) {
        writeAndFail(suffix);
    }

    @Transactional
    public void writeAndFailWithTransaction(final String suffix) {
        writeAndFail(suffix);
    }

    private void writeAndFail(final String suffix) {
        Customer customer = customerRepository.save(new Customer(
                null,
                "Demo Customer " + suffix,
                "demo+" + suffix + "@mail.com",
                "+375290000" + suffix.substring(0, 4),
                new ArrayList<>()));

        Category category = categoryRepository.save(new Category(null, "Demo Category " + suffix, new ArrayList<>()));

        Restaurant restaurant = restaurantRepository.save(new Restaurant(
                null,
                "Demo Restaurant " + suffix,
                "restaurant+" + suffix + "@mail.com",
                "Minsk",
                "Lenina 1",
                "+375170000" + suffix.substring(0, 4),
                true,
                new ArrayList<>()));

        Meal mealOne = mealRepository.save(new Meal(
                null,
                "Demo Meal A " + suffix,
                new BigDecimal("10.50"),
                15,
                category,
                restaurant,
                new LinkedHashSet<>()));

        Meal mealTwo = mealRepository.save(new Meal(
                null,
                "Demo Meal B " + suffix,
                new BigDecimal("8.40"),
                12,
                category,
                restaurant,
                new LinkedHashSet<>()));

        orderService.create(new OrderRequest(
                new BigDecimal("18.90"),
                LocalDateTime.now(),
                "Demo order for transaction test " + suffix,
                customer.getId(),
                Set.of(mealOne.getId(), mealTwo.getId())));

        throw new IllegalStateException("Planned demo exception to compare transactional behavior");
    }
}
