package com.order.tracker.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Set;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Order response payload.")
public class OrderResponse {

    private Long id;
    private BigDecimal amount;
    private LocalDateTime date;
    private String description;
    private Long customerId;
    private String customerName;
    private Set<Long> mealIds;
    private Set<String> mealNames;
}
