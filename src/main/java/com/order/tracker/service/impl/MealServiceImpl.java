package com.order.tracker.service.impl;

import com.order.tracker.domain.Category;
import com.order.tracker.domain.Meal;
import com.order.tracker.domain.Restaurant;
import com.order.tracker.dto.request.MealRequest;
import com.order.tracker.dto.response.MealResponse;
import com.order.tracker.mapper.MealMapper;
import com.order.tracker.repository.CategoryRepository;
import com.order.tracker.repository.MealRepository;
import com.order.tracker.repository.RestaurantRepository;
import com.order.tracker.service.MealService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;

@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
public class MealServiceImpl implements MealService {

    private final MealRepository mealRepository;
    private final CategoryRepository categoryRepository;
    private final RestaurantRepository restaurantRepository;
    private final MealMapper mealMapper;

    @Override
    @Transactional
    public MealResponse create(final MealRequest request) {
        Meal meal = new Meal();
        apply(meal, request);
        return mealMapper.toResponse(mealRepository.save(meal));
    }

    @Override
    public MealResponse getById(final Long id) {
        return mealMapper.toResponse(findMeal(id));
    }

    @Override
    public List<MealResponse> getAll() {
        return mealRepository.findAll().stream()
                .map(mealMapper::toResponse)
                .toList();
    }

    @Override
    @Transactional
    public MealResponse update(final Long id, final MealRequest request) {
        Meal meal = findMeal(id);
        apply(meal, request);
        return mealMapper.toResponse(mealRepository.save(meal));
    }

    @Override
    @Transactional
    public void delete(final Long id) {
        if (!mealRepository.existsById(id)) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Meal not found: " + id);
        }
        mealRepository.deleteById(id);
    }

    private Meal findMeal(final Long id) {
        return mealRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Meal not found: " + id));
    }

    private Category findCategory(final Long id) {
        return categoryRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Category not found: " + id));
    }

    private Restaurant findRestaurant(final Long id) {
        return restaurantRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Restaurant not found: " + id));
    }

    private void apply(final Meal meal, final MealRequest request) {
        meal.setName(request.getName());
        meal.setPrice(request.getPrice());
        meal.setCookingTime(request.getCookingTime());
        meal.setCategory(findCategory(request.getCategoryId()));
        meal.setRestaurant(findRestaurant(request.getRestaurantId()));
    }
}
