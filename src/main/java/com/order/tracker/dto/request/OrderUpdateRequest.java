package com.order.tracker.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Set;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Request body for partially updating an order.")
public class OrderUpdateRequest {

    private LocalDateTime date;

    @DecimalMin(value = "0.01")
    private BigDecimal amount;

    @Pattern(regexp = "\\s*+\\S.*", message = "must not be blank")
    @Size(max = 255)
    private String description;

    @Positive
    private Long customerId;

    private Set<@Positive(message = "Meal id must be positive") Long> mealIds;
}
