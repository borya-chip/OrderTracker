package com.order.tracker.service;

import com.order.tracker.dto.request.RestaurantRequest;
import com.order.tracker.dto.response.RestaurantResponse;

import java.util.List;

public interface RestaurantService {

    RestaurantResponse create(RestaurantRequest request);

    RestaurantResponse getById(Long id);

    List<RestaurantResponse> getAll();

    RestaurantResponse update(Long id, RestaurantRequest request);

    void delete(Long id);
}
