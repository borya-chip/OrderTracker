package com.order.tracker.dto.request;

import java.math.BigDecimal;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class MealRequest {

    @NotBlank(message = "Meal name is required")
    @Size(max = 150, message = "Meal name must be at most 150 characters")
    private String name;

    @NotNull(message = "Price is required")
    @DecimalMin(value = "0.01", message = "Price must be at least 0.01")
    private BigDecimal price;

    @NotNull(message = "Cooking time is required")
    @Positive(message = "Cooking time must be positive")
    private Integer cookingTime;

    @NotNull(message = "Category id is required")
    @Positive(message = "Category id must be positive")
    private Long categoryId;

    @NotNull(message = "Restaurant id is required")
    @Positive(message = "Restaurant id must be positive")
    private Long restaurantId;
}
