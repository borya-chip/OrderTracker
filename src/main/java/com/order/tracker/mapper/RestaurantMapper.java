package com.order.tracker.mapper;

import com.order.tracker.domain.Restaurant;
import com.order.tracker.dto.response.RestaurantResponse;
import org.springframework.stereotype.Component;

@Component
public class RestaurantMapper {

    public RestaurantResponse toResponse(final Restaurant restaurant) {
        if (restaurant == null) {
            return null;
        }
        return new RestaurantResponse(
                restaurant.getId(),
                restaurant.getName(),
                restaurant.getContactEmail(),
                restaurant.getCity(),
                restaurant.getAddress(),
                restaurant.getPhone(),
                restaurant.getActive());
    }
}
