package com.order.tracker.service.impl;

import com.order.tracker.domain.Category;
import com.order.tracker.dto.request.CategoryRequest;
import com.order.tracker.dto.response.CategoryResponse;
import com.order.tracker.mapper.CategoryMapper;
import com.order.tracker.repository.CategoryRepository;
import com.order.tracker.service.CategoryService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;

@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
public class CategoryServiceImpl implements CategoryService {

    private final CategoryRepository categoryRepository;
    private final CategoryMapper categoryMapper;

    @Override
    @Transactional
    public CategoryResponse create(final CategoryRequest request) {
        Category category = new Category();
        apply(category, request);
        return categoryMapper.toResponse(categoryRepository.save(category));
    }

    @Override
    public CategoryResponse getById(final Long id) {
        return categoryMapper.toResponse(findCategory(id));
    }

    @Override
    public List<CategoryResponse> getAll() {
        return categoryRepository.findAll().stream()
                .map(categoryMapper::toResponse)
                .toList();
    }

    @Override
    @Transactional
    public CategoryResponse update(final Long id, final CategoryRequest request) {
        Category category = findCategory(id);
        apply(category, request);
        return categoryMapper.toResponse(categoryRepository.save(category));
    }

    @Override
    @Transactional
    public void delete(final Long id) {
        if (!categoryRepository.existsById(id)) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Category not found: " + id);
        }
        categoryRepository.deleteById(id);
    }

    private Category findCategory(final Long id) {
        return categoryRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Category not found: " + id));
    }

    private void apply(final Category category, final CategoryRequest request) {
        category.setName(request.getName());
    }
}
