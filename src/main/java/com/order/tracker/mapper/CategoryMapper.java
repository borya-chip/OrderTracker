package com.order.tracker.mapper;

import com.order.tracker.domain.Category;
import com.order.tracker.dto.response.CategoryResponse;
import org.springframework.stereotype.Component;

@Component
public class CategoryMapper {

    public CategoryResponse toResponse(final Category category) {
        if (category == null) {
            return null;
        }
        return new CategoryResponse(category.getId(), category.getName());
    }
}
