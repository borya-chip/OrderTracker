package com.order.tracker.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;
import java.math.BigDecimal;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Meal response payload.")
public class MealResponse {

    private Long id;
    private String name;
    private BigDecimal price;
    private Integer cookingTime;
    private Long categoryId;
    private String categoryName;
    private Long restaurantId;
    private String restaurantName;
}
