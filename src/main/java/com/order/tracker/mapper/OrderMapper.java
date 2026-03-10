package com.order.tracker.mapper;

import com.order.tracker.domain.Meal;
import com.order.tracker.domain.Order;
import com.order.tracker.dto.response.OrderResponse;
import org.springframework.stereotype.Component;

import java.util.LinkedHashSet;
import java.util.Set;

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
                entity.getCustomer() != null ? entity.getCustomer().getFullName() : null,
                mealIds,
                mealNames);
    }
}
