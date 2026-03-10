package com.order.tracker.dto.response;

import java.math.BigDecimal;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
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
