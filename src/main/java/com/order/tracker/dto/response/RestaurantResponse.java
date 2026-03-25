package com.order.tracker.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Restaurant response payload.")
public class RestaurantResponse {

    private Long id;
    private String name;
    private String contactEmail;
    private String city;
    private String address;
    private String phone;
    private Boolean active;
}
