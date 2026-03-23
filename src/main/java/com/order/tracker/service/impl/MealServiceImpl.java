package com.order.tracker.service.impl;

import com.order.tracker.cache.CacheManager;
import com.order.tracker.domain.Category;
import com.order.tracker.domain.Meal;
import com.order.tracker.domain.Restaurant;
import com.order.tracker.dto.request.MealRequest;
import com.order.tracker.dto.response.MealResponse;
import com.order.tracker.exception.ConflictException;
import com.order.tracker.exception.ResourceNotFoundException;
import com.order.tracker.mapper.MealMapper;
import com.order.tracker.repository.CategoryRepository;
import com.order.tracker.repository.MealRepository;
import com.order.tracker.repository.RestaurantRepository;
import com.order.tracker.service.MealService;
import lombok.RequiredArgsConstructor;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
public class MealServiceImpl implements MealService {

    private final MealRepository mealRepository;
    private final CategoryRepository categoryRepository;
    private final RestaurantRepository restaurantRepository;
    private final MealMapper mealMapper;
    private final CacheManager cacheManager;

    @Override
    @Transactional
    public MealResponse create(final MealRequest request) {
        Meal meal = new Meal();
        apply(meal, request);
        Meal saved = mealRepository.save(meal);
        invalidateSearchCache();
        return mealMapper.toResponse(saved);
    }

    @Override
    public MealResponse getById(final Long id) {
        return mealMapper.toResponse(findMeal(id));
    }

    @Override
    public Page<MealResponse> getAll(final Pageable pageable) {
        return mealRepository.findAll(pageable)
                .map(mealMapper::toResponse);
    }

    @Override
    @Transactional
    public MealResponse update(final Long id, final MealRequest request) {
        Meal meal = findMeal(id);
        apply(meal, request);
        Meal saved = mealRepository.save(meal);
        invalidateSearchCache();
        return mealMapper.toResponse(saved);
    }

    @Override
    @Transactional
    public void delete(final Long id) {
        if (!mealRepository.existsById(id)) {
            throw new ResourceNotFoundException("Meal not found: " + id);
        }
        try {
            mealRepository.deleteById(id);
            mealRepository.flush();
            invalidateSearchCache();
        } catch (DataIntegrityViolationException ex) {
            throw new ConflictException(
                    "Meal is used in existing orders and cannot be deleted");
        }
    }

    private Meal findMeal(final Long id) {
        return mealRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Meal not found: " + id));
    }

    private Category findCategory(final Long id) {
        return categoryRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Category not found: " + id));
    }

    private Restaurant findRestaurant(final Long id) {
        return restaurantRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Restaurant not found: " + id));
    }

    private void apply(final Meal meal, final MealRequest request) {
        meal.setName(request.getName());
        meal.setPrice(request.getPrice());
        meal.setCookingTime(request.getCookingTime());
        meal.setCategory(findCategory(request.getCategoryId()));
        meal.setRestaurant(findRestaurant(request.getRestaurantId()));
    }

    private void invalidateSearchCache() {
        cacheManager.invalidate(Restaurant.class, Meal.class, Category.class);
    }
}
