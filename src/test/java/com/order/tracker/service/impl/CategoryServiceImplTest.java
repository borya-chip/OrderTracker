package com.order.tracker.service.impl;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.order.tracker.cache.CacheManager;
import com.order.tracker.domain.Category;
import com.order.tracker.domain.Meal;
import com.order.tracker.domain.Restaurant;
import com.order.tracker.dto.request.CategoryRequest;
import com.order.tracker.exception.DuplicateResourceException;
import com.order.tracker.exception.ResourceNotFoundException;
import com.order.tracker.mapper.CategoryMapper;
import com.order.tracker.repository.CategoryRepository;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.dao.DataIntegrityViolationException;

@ExtendWith(MockitoExtension.class)
class CategoryServiceImplTest {

    @Mock
    private CategoryRepository categoryRepository;

    private CacheManager cacheManager;
    private CategoryServiceImpl service;

    @BeforeEach
    void setUp() {
        cacheManager = spy(new CacheManager());
        service = new CategoryServiceImpl(categoryRepository, new CategoryMapper(), cacheManager);
    }

    @Test
    void getByIdShouldReturnMappedCategory() {
        when(categoryRepository.findById(1L)).thenReturn(Optional.of(category(1L, "Pizza")));

        var response = service.getById(1L);

        assertEquals(1L, response.getId());
        assertEquals("Pizza", response.getName());
    }

    @Test
    void getByIdShouldThrowWhenMissing() {
        when(categoryRepository.findById(1L)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> service.getById(1L));
    }

    @Test
    void getAllShouldReturnMappedCategories() {
        when(categoryRepository.findAll()).thenReturn(List.of(category(1L, "Pizza"), category(2L, "Dessert")));

        var response = service.getAll();

        assertEquals(2, response.size());
        assertEquals("Dessert", response.get(1).getName());
    }

    @Test
    void createShouldSaveCategoryAndInvalidateCache() {
        when(categoryRepository.saveAndFlush(any(Category.class))).thenAnswer(invocation -> {
            Category category = invocation.getArgument(0);
            category.setId(3L);
            return category;
        });

        var response = service.create(new CategoryRequest("Drinks"));

        assertEquals(3L, response.getId());
        assertEquals("Drinks", response.getName());
        verify(cacheManager).invalidate(Restaurant.class, Meal.class, Category.class);
    }

    @Test
    void createShouldThrowDuplicateExceptionWhenNameExists() {
        when(categoryRepository.saveAndFlush(any(Category.class)))
                .thenThrow(new DataIntegrityViolationException("duplicate"));

        assertThrows(DuplicateResourceException.class, () -> service.create(new CategoryRequest("Drinks")));
    }

    @Test
    void updateShouldSaveCategoryAndInvalidateCache() {
        Category existing = category(5L, "Old");
        when(categoryRepository.findById(5L)).thenReturn(Optional.of(existing));
        when(categoryRepository.saveAndFlush(any(Category.class))).thenAnswer(invocation -> invocation.getArgument(0));

        var response = service.update(5L, new CategoryRequest("New"));

        assertEquals("New", response.getName());
        verify(cacheManager).invalidate(Restaurant.class, Meal.class, Category.class);
    }

    @Test
    void deleteShouldDeleteExistingCategory() {
        when(categoryRepository.existsById(7L)).thenReturn(true);

        service.delete(7L);

        verify(categoryRepository).deleteById(7L);
        verify(cacheManager).invalidate(Restaurant.class, Meal.class, Category.class);
    }

    @Test
    void deleteShouldThrowWhenMissing() {
        when(categoryRepository.existsById(7L)).thenReturn(false);

        assertThrows(ResourceNotFoundException.class, () -> service.delete(7L));

        verify(categoryRepository, never()).deleteById(any());
    }

    private static Category category(final Long id, final String name) {
        Category category = new Category();
        category.setId(id);
        category.setName(name);
        return category;
    }
}
