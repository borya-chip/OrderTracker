package com.order.tracker.service;

import com.order.tracker.dto.request.RestaurantRequest;
import com.order.tracker.dto.response.RestaurantResponse;
import java.math.BigDecimal;
import java.util.List;

public interface RestaurantService {

    RestaurantResponse create(RestaurantRequest request);

    RestaurantResponse getById(Long id);

    List<RestaurantResponse> getAll();

    List<RestaurantResponse> searchRestaurantsByCategoryWithJpql(
            String categoryName, BigDecimal minMealPrice, BigDecimal maxMealPrice);

    List<RestaurantResponse> searchRestaurantsByCategoryWithNative(
            String categoryName, BigDecimal minMealPrice, BigDecimal maxMealPrice);

    RestaurantResponse update(Long id, RestaurantRequest request);

    void delete(Long id);
}
