package com.order.tracker.controller;

import com.order.tracker.dto.request.MealRequest;
import com.order.tracker.dto.response.MealResponse;
import com.order.tracker.service.MealService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/meals")
public class MealController {

    private final MealService mealService;

    @PostMapping
    public ResponseEntity<MealResponse> create(@Valid @RequestBody final MealRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(mealService.create(request));
    }

    @GetMapping("/{id}")
    public ResponseEntity<MealResponse> getById(@PathVariable final Long id) {
        return ResponseEntity.ok(mealService.getById(id));
    }

    @GetMapping
    public ResponseEntity<List<MealResponse>> getAll() {
        return ResponseEntity.ok(mealService.getAll());
    }

    @PutMapping("/{id}")
    public ResponseEntity<MealResponse> update(
            @PathVariable final Long id,
            @Valid @RequestBody final MealRequest request) {
        return ResponseEntity.ok(mealService.update(id, request));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable final Long id) {
        mealService.delete(id);
        return ResponseEntity.noContent().build();
    }
}
