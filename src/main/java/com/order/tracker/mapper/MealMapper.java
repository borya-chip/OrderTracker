package com.order.tracker.mapper;

import com.order.tracker.domain.Category;
import com.order.tracker.domain.Meal;
import com.order.tracker.domain.Restaurant;
import com.order.tracker.dto.response.MealResponse;
import org.springframework.stereotype.Component;

@Component
public class MealMapper {

    public MealResponse toResponse(final Meal meal) {
        if (meal == null) {
            return null;
        }

        Category category = meal.getCategory();
        Restaurant restaurant = meal.getRestaurant();

        return new MealResponse(
                meal.getId(),
                meal.getName(),
                meal.getPrice(),
                meal.getCookingTime(),
                category != null ? category.getId() : null,
                category != null ? category.getName() : null,
                restaurant != null ? restaurant.getId() : null,
                restaurant != null ? restaurant.getName() : null);
    }
}
