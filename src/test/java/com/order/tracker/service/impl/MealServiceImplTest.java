package com.order.tracker.service.impl;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.order.tracker.cache.CacheManager;
import com.order.tracker.domain.Category;
import com.order.tracker.domain.Meal;
import com.order.tracker.domain.Restaurant;
import com.order.tracker.dto.request.MealRequest;
import com.order.tracker.exception.ConflictException;
import com.order.tracker.exception.ResourceNotFoundException;
import com.order.tracker.mapper.MealMapper;
import com.order.tracker.repository.CategoryRepository;
import com.order.tracker.repository.MealRepository;
import com.order.tracker.repository.RestaurantRepository;
import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;

@ExtendWith(MockitoExtension.class)
class MealServiceImplTest {

    @Mock
    private MealRepository mealRepository;

    @Mock
    private CategoryRepository categoryRepository;

    @Mock
    private RestaurantRepository restaurantRepository;

    private CacheManager cacheManager;
    private MealServiceImpl service;

    @BeforeEach
    void setUp() {
        cacheManager = spy(new CacheManager());
        service = new MealServiceImpl(
                mealRepository,
                categoryRepository,
                restaurantRepository,
                new MealMapper(),
                cacheManager);
    }

    @Test
    void getByIdShouldReturnMappedMeal() {
        when(mealRepository.findById(1L)).thenReturn(Optional.of(meal(1L, category(2L), restaurant(3L))));

        var response = service.getById(1L);

        assertEquals(1L, response.getId());
        assertEquals(2L, response.getCategoryId());
        assertEquals(3L, response.getRestaurantId());
    }

    @Test
    void getByIdShouldThrowWhenMissing() {
        when(mealRepository.findById(1L)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> service.getById(1L));
    }

    @Test
    void getAllShouldReturnMappedPage() {
        when(mealRepository.findAll(PageRequest.of(0, 2))).thenReturn(
                new PageImpl<>(List.of(meal(1L, category(2L), restaurant(3L)))));

        var page = service.getAll(PageRequest.of(0, 2));

        assertEquals(1, page.getContent().size());
        assertEquals("Meal 1", page.getContent().get(0).getName());
    }

    @Test
    void createShouldSaveMealAndInvalidateCache() {
        MealRequest request = new MealRequest("Soup", new BigDecimal("12.50"), 20, 2L, 3L);
        when(categoryRepository.findById(2L)).thenReturn(Optional.of(category(2L)));
        when(restaurantRepository.findById(3L)).thenReturn(Optional.of(restaurant(3L)));
        when(mealRepository.save(any(Meal.class))).thenAnswer(invocation -> {
            Meal meal = invocation.getArgument(0);
            meal.setId(9L);
            return meal;
        });

        var response = service.create(request);

        assertEquals(9L, response.getId());
        assertEquals("Soup", response.getName());
        verify(cacheManager).invalidate(Restaurant.class, Meal.class, Category.class);
    }

    @Test
    void createShouldThrowWhenCategoryIsMissing() {
        MealRequest request = new MealRequest("Soup", new BigDecimal("12.50"), 20, 2L, 3L);
        when(categoryRepository.findById(2L)).thenReturn(Optional.empty());

        assertThrows(
                ResourceNotFoundException.class,
                () -> service.create(request));

        verify(mealRepository, never()).save(any(Meal.class));
    }

    @Test
    void updateShouldSaveUpdatedMealAndInvalidateCache() {
        Meal existing = meal(5L, category(2L), restaurant(3L));
        when(mealRepository.findById(5L)).thenReturn(Optional.of(existing));
        when(categoryRepository.findById(4L)).thenReturn(Optional.of(category(4L)));
        when(restaurantRepository.findById(6L)).thenReturn(Optional.of(restaurant(6L)));
        when(mealRepository.save(any(Meal.class))).thenAnswer(invocation -> invocation.getArgument(0));

        var response = service.update(5L, new MealRequest("Pasta", new BigDecimal("15.00"), 25, 4L, 6L));

        assertEquals("Pasta", response.getName());
        assertEquals(4L, response.getCategoryId());
        assertEquals(6L, response.getRestaurantId());
        verify(cacheManager).invalidate(Restaurant.class, Meal.class, Category.class);
    }

    @Test
    void deleteShouldDeleteExistingMeal() {
        when(mealRepository.existsById(7L)).thenReturn(true);

        service.delete(7L);

        verify(mealRepository).deleteById(7L);
        verify(mealRepository).flush();
        verify(cacheManager).invalidate(Restaurant.class, Meal.class, Category.class);
    }

    @Test
    void deleteShouldThrowConflictWhenMealIsUsedInOrders() {
        when(mealRepository.existsById(7L)).thenReturn(true);
        doThrow(new DataIntegrityViolationException("fk")).when(mealRepository).flush();

        assertThrows(ConflictException.class, () -> service.delete(7L));
    }

    private static Category category(final Long id) {
        Category category = new Category();
        category.setId(id);
        category.setName("Category " + id);
        return category;
    }

    private static Restaurant restaurant(final Long id) {
        Restaurant restaurant = new Restaurant();
        restaurant.setId(id);
        restaurant.setName("Restaurant " + id);
        restaurant.setActive(true);
        return restaurant;
    }

    private static Meal meal(final Long id, final Category category, final Restaurant restaurant) {
        Meal meal = new Meal();
        meal.setId(id);
        meal.setName("Meal " + id);
        meal.setPrice(new BigDecimal("11.00"));
        meal.setCookingTime(15);
        meal.setCategory(category);
        meal.setRestaurant(restaurant);
        return meal;
    }
}
