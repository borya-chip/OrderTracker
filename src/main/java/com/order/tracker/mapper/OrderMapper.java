package com.order.tracker.mapper;

import com.order.tracker.domain.Customer;
import com.order.tracker.domain.Meal;
import com.order.tracker.domain.Order;
import com.order.tracker.dto.request.OrderRequest;
import com.order.tracker.dto.response.OrderResponse;
import java.util.LinkedHashSet;
import java.util.Set;
import org.springframework.stereotype.Component;

@Component
public class OrderMapper {

    public OrderResponse toResponse(final Order entity) {
        if (entity == null) {
            return null;
        }

        Set<Long> mealIds = entity.getMeals().stream()
                .map(Meal::getId)
                .collect(java.util.stream.Collectors.toCollection(LinkedHashSet::new));

        Set<String> mealNames = entity.getMeals().stream()
                .map(Meal::getName)
                .collect(java.util.stream.Collectors.toCollection(LinkedHashSet::new));

        return new OrderResponse(
                entity.getId(),
                entity.getAmount(),
                entity.getDate(),
                entity.getDescription(),
                entity.getCustomer() != null ? entity.getCustomer().getId() : null,
                entity.getCustomer() != null
                        ? entity.getCustomer().getFirstName() + " " + entity.getCustomer().getLastName()
                        : null,
                mealIds,
                mealNames);
    }

    public Order fromRequest(final OrderRequest request, final Customer customer, final Set<Meal> meals) {
        if (request == null) {
            return null;
        }

        Order order = new Order();
        order.setAmount(request.getAmount());
        order.setDate(request.getDate());
        order.setDescription(request.getDescription());
        order.setCustomer(customer);
        order.setMeals(meals != null ? new LinkedHashSet<>(meals) : new LinkedHashSet<>());
        return order;
    }
}
