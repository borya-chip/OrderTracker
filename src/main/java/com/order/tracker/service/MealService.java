package com.order.tracker.service;

import com.order.tracker.dto.request.MealRequest;
import com.order.tracker.dto.response.MealResponse;

import java.util.List;

public interface MealService {

    MealResponse create(MealRequest request);

    MealResponse getById(Long id);

    List<MealResponse> getAll();

    MealResponse update(Long id, MealRequest request);

    void delete(Long id);
}
