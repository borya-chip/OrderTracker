package com.order.tracker.controller.api;

import com.order.tracker.dto.request.RestaurantRequest;
import com.order.tracker.dto.response.RestaurantResponse;
import com.order.tracker.exception.response.ErrorResponse;
import com.order.tracker.exception.response.ValidationErrorResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.ArraySchema;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import java.math.BigDecimal;
import java.util.List;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;

@Tag(name = "Restaurant Controller", description = "Restaurant management endpoints")
public interface RestaurantControllerApi {

    @Operation(summary = "Get restaurant by ID", description = "Returns restaurant details by identifier.")
    @ApiResponses(value = {
        @ApiResponse(
                responseCode = "200",
                description = "Restaurant found",
                content = @Content(schema = @Schema(implementation = RestaurantResponse.class))),
        @ApiResponse(
                responseCode = "404",
                description = "Restaurant not found",
                content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    })
    @GetMapping("/api/v1/restaurants/{id}")
    ResponseEntity<RestaurantResponse> getById(
            @Parameter(description = "Restaurant ID", required = true, example = "1")
            @PathVariable Long id
    );

    @Operation(summary = "Get all restaurants", description = "Returns all restaurants.")
    @ApiResponses(value = {
        @ApiResponse(
                responseCode = "200",
                description = "Restaurants retrieved successfully",
                content = @Content(array = @ArraySchema(schema = @Schema(implementation = RestaurantResponse.class))))
    })
    @GetMapping("/api/v1/restaurants")
    ResponseEntity<List<RestaurantResponse>> getAll();

    @Operation(
            summary = "Search restaurants with JPQL",
            description = "Finds restaurants by category name and meal price range using JPQL."
    )
    @ApiResponses(value = {
        @ApiResponse(
                responseCode = "200",
                description = "Restaurants retrieved successfully",
                content = @Content(array = @ArraySchema(schema = @Schema(implementation = RestaurantResponse.class)))),
        @ApiResponse(
                responseCode = "400",
                description = "Invalid search parameters",
                content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    })
    @GetMapping("/api/v1/restaurants/search/category/jpql")
    ResponseEntity<List<RestaurantResponse>> searchRestaurantsWithJpql(
            @Parameter(description = "Category name filter", required = true, example = "Burgers")
            @RequestParam("categoryName") String categoryName,
            @Parameter(description = "Minimum meal price", required = true, example = "10.00")
            @RequestParam("minMealPrice") BigDecimal minMealPrice,
            @Parameter(description = "Maximum meal price", required = true, example = "40.00")
            @RequestParam("maxMealPrice") BigDecimal maxMealPrice
    );

    @Operation(
            summary = "Search restaurants with native SQL",
            description = "Finds restaurants by category name and meal price range using native SQL."
    )
    @ApiResponses(value = {
        @ApiResponse(
                responseCode = "200",
                description = "Restaurants retrieved successfully",
                content = @Content(array = @ArraySchema(schema = @Schema(implementation = RestaurantResponse.class)))),
        @ApiResponse(
                responseCode = "400",
                description = "Invalid search parameters",
                content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    })
    @GetMapping("/api/v1/restaurants/search/category/native")
    ResponseEntity<List<RestaurantResponse>> searchRestaurantsWithNative(
            @Parameter(description = "Category name filter", required = true, example = "Burgers")
            @RequestParam("categoryName") String categoryName,
            @Parameter(description = "Minimum meal price", required = true, example = "10.00")
            @RequestParam("minMealPrice") BigDecimal minMealPrice,
            @Parameter(description = "Maximum meal price", required = true, example = "40.00")
            @RequestParam("maxMealPrice") BigDecimal maxMealPrice
    );

    @Operation(summary = "Create restaurant", description = "Creates a new restaurant.")
    @ApiResponses(value = {
        @ApiResponse(
                responseCode = "201",
                description = "Restaurant created successfully",
                content = @Content(schema = @Schema(implementation = RestaurantResponse.class))),
        @ApiResponse(
                responseCode = "400",
                description = "Invalid request body",
                content = @Content(schema = @Schema(implementation = ValidationErrorResponse.class))),
        @ApiResponse(
                responseCode = "409",
                description = "Duplicate restaurant name",
                content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    })
    @PostMapping("/api/v1/restaurants")
    ResponseEntity<RestaurantResponse> create(
            @Parameter(description = "Restaurant payload", required = true)
            @io.swagger.v3.oas.annotations.parameters.RequestBody(
                    required = true,
                    description = "Demo restaurant for transactional bulk import scenario.",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = RestaurantRequest.class),
                            examples = @ExampleObject(
                                    name = "Transactional demo restaurant",
                                    value = """
                                            {
                                              "name": "Transactional Demo Restaurant",
                                              "contactEmail": "tx.demo.restaurant@example.com",
                                              "city": "Minsk",
                                              "address": "Main street 1",
                                              "phone": "+375291110000",
                                              "active": true
                                            }
                                            """)))
            @Valid @RequestBody RestaurantRequest request
    );

    @Operation(summary = "Update restaurant", description = "Updates an existing restaurant.")
    @ApiResponses(value = {
        @ApiResponse(
                responseCode = "200",
                description = "Restaurant updated successfully",
                content = @Content(schema = @Schema(implementation = RestaurantResponse.class))),
        @ApiResponse(
                responseCode = "400",
                description = "Invalid request body",
                content = @Content(schema = @Schema(implementation = ValidationErrorResponse.class))),
        @ApiResponse(
                responseCode = "404",
                description = "Restaurant not found",
                content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
        @ApiResponse(
                responseCode = "409",
                description = "Duplicate restaurant name",
                content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    })
    @PutMapping("/api/v1/restaurants/{id}")
    ResponseEntity<RestaurantResponse> update(
            @Parameter(description = "Restaurant ID", required = true, example = "1")
            @PathVariable Long id,
            @Parameter(description = "Restaurant payload", required = true)
            @Valid @RequestBody RestaurantRequest request
    );

    @Operation(summary = "Delete restaurant", description = "Deletes a restaurant by ID.")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "204", description = "Restaurant deleted successfully"),
        @ApiResponse(
                responseCode = "404",
                description = "Restaurant not found",
                content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    })
    @DeleteMapping("/api/v1/restaurants/{id}")
    ResponseEntity<Void> delete(
            @Parameter(description = "Restaurant ID", required = true, example = "1")
            @PathVariable Long id
    );
}
