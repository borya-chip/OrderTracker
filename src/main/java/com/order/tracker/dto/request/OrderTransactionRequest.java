package com.order.tracker.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Request body for creating order transaction flow.")
public class OrderTransactionRequest {

    @NotNull
    @Positive
    private Long customerId;

    @NotNull
    @Positive
    private Long categoryId;

    @NotNull
    @Positive
    private Long restaurantId;

    @NotNull
    @DecimalMin(value = "0.01")
    private BigDecimal amount;

    private LocalDateTime orderedAt;

    @Size(max = 255)
    private String description;
}
