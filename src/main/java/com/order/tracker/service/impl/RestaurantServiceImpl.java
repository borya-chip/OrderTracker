package com.order.tracker.service.impl;

import com.order.tracker.cache.CacheKey;
import com.order.tracker.cache.CacheManager;
import com.order.tracker.domain.Category;
import com.order.tracker.domain.Meal;
import com.order.tracker.domain.Restaurant;
import com.order.tracker.dto.request.RestaurantRequest;
import com.order.tracker.dto.response.RestaurantResponse;
import com.order.tracker.exception.ConflictException;
import com.order.tracker.exception.DuplicateResourceException;
import com.order.tracker.exception.ResourceNotFoundException;
import com.order.tracker.mapper.RestaurantMapper;
import com.order.tracker.repository.RestaurantRepository;
import com.order.tracker.service.RestaurantService;
import java.math.BigDecimal;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
public class RestaurantServiceImpl implements RestaurantService {

    private final RestaurantRepository restaurantRepository;
    private final RestaurantMapper restaurantMapper;
    private final CacheManager cacheManager;

    @Override
    @Transactional
    public RestaurantResponse create(final RestaurantRequest request) {
        Restaurant restaurant = new Restaurant();
        apply(restaurant, request);
        Restaurant saved = saveRestaurant(restaurant);
        invalidateSearchCache();
        return restaurantMapper.toResponse(saved);
    }

    @Override
    public RestaurantResponse getById(final Long id) {
        CacheKey cacheKey = buildCacheKey("getById", id);
        return cacheManager.computeIfAbsent(cacheKey, () -> restaurantMapper.toResponse(findRestaurant(id)));
    }

    @Override
    public List<RestaurantResponse> getAll() {
        CacheKey cacheKey = buildCacheKey("getAll");
        return cacheManager.computeIfAbsent(cacheKey, () -> restaurantRepository.findAll().stream()
                .map(restaurantMapper::toResponse)
                .toList());
    }

    @Override
    public List<RestaurantResponse> searchRestaurantsByCategoryWithJpql(
            final String categoryName,
            final BigDecimal minMealPrice,
            final BigDecimal maxMealPrice) {
        CacheKey cacheKey = buildSearchCacheKey(
                "searchRestaurantsByCategoryWithJpql", categoryName, minMealPrice, maxMealPrice);
        return cacheManager.computeIfAbsent(cacheKey, () -> restaurantRepository
                .findRestaurantsByCategoryWithJpql(categoryName, minMealPrice, maxMealPrice)
                .stream()
                .map(restaurantMapper::toResponse)
                .toList());
    }

    @Override
    public List<RestaurantResponse> searchRestaurantsByCategoryWithNative(
            final String categoryName,
            final BigDecimal minMealPrice,
            final BigDecimal maxMealPrice) {
        CacheKey cacheKey = buildSearchCacheKey(
                "searchRestaurantsByCategoryWithNative", categoryName, minMealPrice, maxMealPrice);
        return cacheManager.computeIfAbsent(cacheKey, () -> restaurantRepository
                .findRestaurantsByCategoryWithNative(categoryName, minMealPrice, maxMealPrice)
                .stream()
                .map(restaurantMapper::toResponse)
                .toList());
    }

    @Override
    @Transactional
    public RestaurantResponse update(final Long id, final RestaurantRequest request) {
        Restaurant restaurant = findRestaurant(id);
        apply(restaurant, request);
        Restaurant saved = saveRestaurant(restaurant);
        invalidateSearchCache();
        return restaurantMapper.toResponse(saved);
    }

    @Override
    @Transactional
    public void delete(final Long id) {
        if (!restaurantRepository.existsById(id)) {
            throw new ResourceNotFoundException("Restaurant not found: " + id);
        }
        try {
            restaurantRepository.deleteById(id);
            restaurantRepository.flush();
            invalidateSearchCache();
        } catch (DataIntegrityViolationException ex) {
            throw new ConflictException(
                    "Restaurant has related meals and cannot be deleted");
        }
    }

    private Restaurant findRestaurant(final Long id) {
        return restaurantRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Restaurant not found: " + id));
    }

    private void apply(final Restaurant restaurant, final RestaurantRequest request) {
        restaurant.setName(request.getName());
        restaurant.setContactEmail(request.getContactEmail());
        restaurant.setCity(request.getCity());
        restaurant.setAddress(request.getAddress());
        restaurant.setPhone(request.getPhone());
        restaurant.setActive(request.getActive() == null ? Boolean.TRUE : request.getActive());
    }

    private Restaurant saveRestaurant(final Restaurant restaurant) {
        try {
            return restaurantRepository.saveAndFlush(restaurant);
        } catch (DataIntegrityViolationException ex) {
            throw new DuplicateResourceException(
                    "Restaurant with name '%s' already exists".formatted(restaurant.getName()));
        }
    }

    private CacheKey buildCacheKey(final String methodName, final Object... args) {
        return new CacheKey(Restaurant.class, methodName, args);
    }

    private CacheKey buildSearchCacheKey(
            final String methodName,
            final String categoryName,
            final BigDecimal minMealPrice,
            final BigDecimal maxMealPrice) {
        return buildCacheKey(methodName, categoryName, minMealPrice, maxMealPrice);
    }

    private void invalidateSearchCache() {
        cacheManager.invalidate(Restaurant.class, Meal.class, Category.class);
    }
}
