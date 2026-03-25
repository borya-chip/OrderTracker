package com.order.tracker.controller;

import com.order.tracker.controller.api.MealControllerApi;
import com.order.tracker.dto.request.MealRequest;
import com.order.tracker.dto.response.MealResponse;
import com.order.tracker.service.MealService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
public class MealController implements MealControllerApi {

    private final MealService mealService;

    @PostMapping("/api/v1/meals")
    public ResponseEntity<MealResponse> create(@Valid @RequestBody final MealRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(mealService.create(request));
    }

    @GetMapping("/api/v1/meals/{id}")
    public ResponseEntity<MealResponse> getById(@PathVariable final Long id) {
        return ResponseEntity.ok(mealService.getById(id));
    }

    @GetMapping("/api/v1/meals")
    public ResponseEntity<Page<MealResponse>> getAll(
            @RequestParam(defaultValue = "0") final int page,
            @RequestParam(defaultValue = "3") final int size,
            @RequestParam(defaultValue = "id") final String sortBy,
            @RequestParam(defaultValue = "true") final boolean ascending) {
        Sort sort = ascending ? Sort.by(sortBy).ascending() : Sort.by(sortBy).descending();
        Pageable pageable = PageRequest.of(page, size, sort);
        return ResponseEntity.ok(mealService.getAll(pageable));
    }

    @PutMapping("/api/v1/meals/{id}")
    public ResponseEntity<MealResponse> update(
            @PathVariable final Long id,
            @Valid @RequestBody final MealRequest request) {
        return ResponseEntity.ok(mealService.update(id, request));
    }

    @DeleteMapping("/api/v1/meals/{id}")
    public ResponseEntity<Void> delete(@PathVariable final Long id) {
        mealService.delete(id);
        return ResponseEntity.noContent().build();
    }
}
