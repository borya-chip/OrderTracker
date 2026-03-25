package com.order.tracker.controller;

import com.order.tracker.controller.api.CategoryControllerApi;
import com.order.tracker.dto.request.CategoryRequest;
import com.order.tracker.dto.response.CategoryResponse;
import com.order.tracker.service.CategoryService;
import jakarta.validation.Valid;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
public class CategoryController implements CategoryControllerApi {

    private final CategoryService categoryService;

    @PostMapping("/api/v1/categories")
    public ResponseEntity<CategoryResponse> create(@Valid @RequestBody final CategoryRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(categoryService.create(request));
    }

    @GetMapping("/api/v1/categories/{id}")
    public ResponseEntity<CategoryResponse> getById(@PathVariable final Long id) {
        return ResponseEntity.ok(categoryService.getById(id));
    }

    @GetMapping("/api/v1/categories")
    public ResponseEntity<List<CategoryResponse>> getAll() {
        return ResponseEntity.ok(categoryService.getAll());
    }

    @PutMapping("/api/v1/categories/{id}")
    public ResponseEntity<CategoryResponse> update(
            @PathVariable final Long id,
            @Valid @RequestBody final CategoryRequest request) {
        return ResponseEntity.ok(categoryService.update(id, request));
    }

    @DeleteMapping("/api/v1/categories/{id}")
    public ResponseEntity<Void> delete(@PathVariable final Long id) {
        categoryService.delete(id);
        return ResponseEntity.noContent().build();
    }
}
