package com.order.tracker.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Request body for creating a restaurant.")
public class RestaurantRequest {

    @NotBlank(message = "Restaurant name is required")
    @Size(max = 120, message = "Restaurant name must be at most 120 characters")
    private String name;

    @Email(message = "Email has invalid format")
    @Size(max = 255, message = "Contact email must be at most 255 characters")
    private String contactEmail;

    @Size(max = 100, message = "City must be at most 100 characters")
    private String city;

    @Size(max = 255, message = "Address must be at most 255 characters")
    private String address;

    @Size(max = 20, message = "Phone must be at most 20 characters")
    private String phone;

    private Boolean active;
}
