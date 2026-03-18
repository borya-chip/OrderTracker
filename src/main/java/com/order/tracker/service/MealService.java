package com.order.tracker.service;

import com.order.tracker.dto.request.MealRequest;
import com.order.tracker.dto.response.MealResponse;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface MealService {

    MealResponse create(MealRequest request);

    MealResponse getById(Long id);

    Page<MealResponse> getAll(Pageable pageable);

    MealResponse update(Long id, MealRequest request);

    void delete(Long id);
}
