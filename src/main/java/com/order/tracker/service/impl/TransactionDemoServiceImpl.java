package com.order.tracker.service.impl;

import com.order.tracker.dto.response.TransactionDemoResponse;
import com.order.tracker.repository.CategoryRepository;
import com.order.tracker.repository.CustomerRepository;
import com.order.tracker.repository.MealRepository;
import com.order.tracker.repository.OrderRepository;
import com.order.tracker.repository.RestaurantRepository;
import com.order.tracker.service.TransactionDemoService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.UUID;

@Service
@RequiredArgsConstructor
public class TransactionDemoServiceImpl implements TransactionDemoService {

    private final TransactionDemoDataWriter dataWriter;
    private final CustomerRepository customerRepository;
    private final CategoryRepository categoryRepository;
    private final RestaurantRepository restaurantRepository;
    private final MealRepository mealRepository;
    private final OrderRepository orderRepository;

    @Override
    public TransactionDemoResponse runWithoutTransactional() {
        Snapshot before = snapshot();
        String error = null;

        try {
            dataWriter.writeAndFailWithoutTransaction(randomSuffix());
        } catch (RuntimeException ex) {
            error = ex.getMessage();
        }

        Snapshot after = snapshot();
        return response("WITHOUT_TRANSACTIONAL", error, before, after);
    }

    @Override
    public TransactionDemoResponse runWithTransactional() {
        Snapshot before = snapshot();
        String error = null;

        try {
            dataWriter.writeAndFailWithTransaction(randomSuffix());
        } catch (RuntimeException ex) {
            error = ex.getMessage();
        }

        Snapshot after = snapshot();
        return response("WITH_TRANSACTIONAL", error, before, after);
    }

    private Snapshot snapshot() {
        return new Snapshot(
                customerRepository.count(),
                categoryRepository.count(),
                restaurantRepository.count(),
                mealRepository.count(),
                orderRepository.count());
    }

    private String randomSuffix() {
        return UUID.randomUUID().toString().replace("-", "").substring(0, 8);
    }

    private TransactionDemoResponse response(
            final String mode,
            final String error,
            final Snapshot before,
            final Snapshot after) {

        return new TransactionDemoResponse(
                mode,
                error,
                after.customers() - before.customers(),
                after.categories() - before.categories(),
                after.restaurants() - before.restaurants(),
                after.meals() - before.meals(),
                after.orders() - before.orders());
    }

    private record Snapshot(long customers, long categories, long restaurants, long meals, long orders) {
    }
}
