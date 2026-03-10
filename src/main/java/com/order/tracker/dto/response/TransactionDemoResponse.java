package com.order.tracker.dto.response;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class TransactionDemoResponse {

    private String mode;
    private String error;
    private long customerDelta;
    private long categoryDelta;
    private long restaurantDelta;
    private long mealDelta;
    private long orderDelta;
}
