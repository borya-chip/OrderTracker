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
import com.order.tracker.dto.request.RestaurantRequest;
import com.order.tracker.exception.ConflictException;
import com.order.tracker.exception.DuplicateResourceException;
import com.order.tracker.exception.ResourceNotFoundException;
import com.order.tracker.mapper.RestaurantMapper;
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

@ExtendWith(MockitoExtension.class)
class RestaurantServiceImplTest {

    @Mock
    private RestaurantRepository restaurantRepository;

    private CacheManager cacheManager;
    private RestaurantServiceImpl service;

    @BeforeEach
    void setUp() {
        cacheManager = spy(new CacheManager());
        service = new RestaurantServiceImpl(restaurantRepository, new RestaurantMapper(), cacheManager);
    }

    @Test
    void getByIdShouldUseCache() {
        when(restaurantRepository.findById(1L)).thenReturn(Optional.of(restaurant(1L, "Roma")));

        var first = service.getById(1L);
        var second = service.getById(1L);

        assertEquals("Roma", first.getName());
        assertEquals("Roma", second.getName());
        verify(restaurantRepository).findById(1L);
    }

    @Test
    void getByIdShouldThrowWhenMissing() {
        when(restaurantRepository.findById(1L)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> service.getById(1L));
    }

    @Test
    void getAllShouldUseCache() {
        when(restaurantRepository.findAll()).thenReturn(List.of(restaurant(1L, "Roma")));

        var first = service.getAll();
        var second = service.getAll();

        assertEquals(1, first.size());
        assertEquals(1, second.size());
        verify(restaurantRepository).findAll();
    }

    @Test
    void searchMethodsShouldUseCache() {
        Restaurant restaurant = restaurant(1L, "Roma");
        when(restaurantRepository.findRestaurantsByCategoryWithJpql(
                "Pizza", new BigDecimal("10.00"), new BigDecimal("30.00"))).thenReturn(List.of(restaurant));
        when(restaurantRepository.findRestaurantsByCategoryWithNative(
                "Pizza", new BigDecimal("10.00"), new BigDecimal("30.00"))).thenReturn(List.of(restaurant));

        var jpqlFirst = service.searchRestaurantsByCategoryWithJpql(
                "Pizza", new BigDecimal("10.00"), new BigDecimal("30.00"));
        var jpqlSecond = service.searchRestaurantsByCategoryWithJpql(
                "Pizza", new BigDecimal("10.00"), new BigDecimal("30.00"));
        var nativeFirst = service.searchRestaurantsByCategoryWithNative(
                "Pizza", new BigDecimal("10.00"), new BigDecimal("30.00"));
        var nativeSecond = service.searchRestaurantsByCategoryWithNative(
                "Pizza", new BigDecimal("10.00"), new BigDecimal("30.00"));

        assertEquals(1, jpqlFirst.size());
        assertEquals(1, jpqlSecond.size());
        assertEquals(1, nativeFirst.size());
        assertEquals(1, nativeSecond.size());
        verify(restaurantRepository).findRestaurantsByCategoryWithJpql(
                "Pizza", new BigDecimal("10.00"), new BigDecimal("30.00"));
        verify(restaurantRepository).findRestaurantsByCategoryWithNative(
                "Pizza", new BigDecimal("10.00"), new BigDecimal("30.00"));
    }

    @Test
    void createShouldSaveRestaurantWithDefaultActiveFlag() {
        RestaurantRequest request = new RestaurantRequest("Roma", "roma@example.com", "Minsk", "Main 1", "+123", null);
        when(restaurantRepository.saveAndFlush(any(Restaurant.class))).thenAnswer(invocation -> {
            Restaurant restaurant = invocation.getArgument(0);
            restaurant.setId(9L);
            return restaurant;
        });

        var response = service.create(request);

        assertEquals(9L, response.getId());
        assertEquals(Boolean.TRUE, response.getActive());
        verify(cacheManager).invalidate(Restaurant.class, Meal.class, Category.class);
    }

    @Test
    void createShouldThrowDuplicateExceptionWhenNameExists() {
        RestaurantRequest request = new RestaurantRequest("Roma", "roma@example.com", "Minsk", "Main 1", "+123", true);
        when(restaurantRepository.saveAndFlush(any(Restaurant.class)))
                .thenThrow(new DataIntegrityViolationException("duplicate"));

        assertThrows(DuplicateResourceException.class, () -> service.create(request));
    }

    @Test
        void updateShouldSaveRestaurantAndInvalidateCache() {
        Restaurant existing = restaurant(5L, "Old");
        when(restaurantRepository.findById(5L)).thenReturn(Optional.of(existing));
        when(restaurantRepository.saveAndFlush(any(Restaurant.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        var response = service.update(
                5L,
                new RestaurantRequest("New", "new@example.com", "Grodno", "Street 2", "+999", false));

        assertEquals("New", response.getName());
        assertEquals(Boolean.FALSE, response.getActive());
        verify(cacheManager).invalidate(Restaurant.class, Meal.class, Category.class);
    }

    @Test
    void deleteShouldThrowWhenMissing() {
        when(restaurantRepository.existsById(7L)).thenReturn(false);

        assertThrows(ResourceNotFoundException.class, () -> service.delete(7L));

        verify(restaurantRepository, never()).deleteById(any());
    }

    @Test
    void deleteShouldThrowConflictWhenRestaurantHasRelatedMeals() {
        when(restaurantRepository.existsById(7L)).thenReturn(true);
        doThrow(new DataIntegrityViolationException("fk")).when(restaurantRepository).flush();

        assertThrows(ConflictException.class, () -> service.delete(7L));
    }

    private static Restaurant restaurant(final Long id, final String name) {
        Restaurant restaurant = new Restaurant();
        restaurant.setId(id);
        restaurant.setName(name);
        restaurant.setContactEmail(name.toLowerCase() + "@example.com");
        restaurant.setCity("Minsk");
        restaurant.setAddress("Address");
        restaurant.setPhone("+123");
        restaurant.setActive(true);
        return restaurant;
    }
}
