package com.order.tracker.controller.api;

import com.order.tracker.dto.request.MealRequest;
import com.order.tracker.dto.response.MealResponse;
import com.order.tracker.exception.response.ErrorResponse;
import com.order.tracker.exception.response.ValidationErrorResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;

@Tag(name = "Meal Controller", description = "Meal management endpoints")
public interface MealControllerApi {

    @Operation(summary = "Get meal by ID", description = "Returns meal details by identifier.")
    @ApiResponses(value = {
        @ApiResponse(
                responseCode = "200",
                description = "Meal found",
                content = @Content(schema = @Schema(implementation = MealResponse.class))),
        @ApiResponse(
                responseCode = "404",
                description = "Meal not found",
                content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    })
    @GetMapping("/api/v1/meals/{id}")
    ResponseEntity<MealResponse> getById(
            @Parameter(description = "Meal ID", required = true, example = "1")
            @PathVariable Long id
    );

    @Operation(summary = "Get meals page", description = "Returns meals with pagination and sorting.")
    @ApiResponses(value = {
        @ApiResponse(
                responseCode = "200",
                description = "Meals retrieved successfully",
                content = @Content(schema = @Schema(implementation = Page.class)))
    })
    @GetMapping("/api/v1/meals")
    ResponseEntity<Page<MealResponse>> getAll(
            @Parameter(description = "Page number", example = "0")
            @RequestParam(defaultValue = "0") int page,
            @Parameter(description = "Page size", example = "10")
            @RequestParam(defaultValue = "3") int size,
            @Parameter(description = "Sort field", example = "id")
            @RequestParam(defaultValue = "id") String sortBy,
            @Parameter(description = "Sort ascending", example = "true")
            @RequestParam(defaultValue = "true") boolean ascending
    );

    @Operation(summary = "Create meal", description = "Creates a new meal.")
    @ApiResponses(value = {
        @ApiResponse(
                responseCode = "201",
                description = "Meal created successfully",
                content = @Content(schema = @Schema(implementation = MealResponse.class))),
        @ApiResponse(
                responseCode = "400",
                description = "Invalid request body",
                content = @Content(schema = @Schema(implementation = ValidationErrorResponse.class))),
        @ApiResponse(
                responseCode = "404",
                description = "Category or restaurant not found",
                content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    })
    @PostMapping("/api/v1/meals")
    ResponseEntity<MealResponse> create(
            @Parameter(description = "Meal payload", required = true)
            @io.swagger.v3.oas.annotations.parameters.RequestBody(
                    required = true,
                    description = """
                            Demo meal for transactional bulk import scenario.
                            Replace categoryId and restaurantId with IDs created
                            in previous steps.
                            """,
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = MealRequest.class),
                            examples = @ExampleObject(
                                    name = "Transactional demo meal",
                                    value = """
                                            {
                                              "name": "Transactional Demo Meal",
                                              "price": 12.50,
                                              "cookingTime": 20,
                                              "categoryId": 1,
                                              "restaurantId": 1
                                            }
                                            """)))
            @Valid @RequestBody MealRequest request
    );

    @Operation(summary = "Update meal", description = "Updates an existing meal.")
    @ApiResponses(value = {
        @ApiResponse(
                responseCode = "200",
                description = "Meal updated successfully",
                content = @Content(schema = @Schema(implementation = MealResponse.class))),
        @ApiResponse(
                responseCode = "400",
                description = "Invalid request body",
                content = @Content(schema = @Schema(implementation = ValidationErrorResponse.class))),
        @ApiResponse(
                responseCode = "404",
                description = "Meal, category or restaurant not found",
                content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    })
    @PutMapping("/api/v1/meals/{id}")
    ResponseEntity<MealResponse> update(
            @Parameter(description = "Meal ID", required = true, example = "1")
            @PathVariable Long id,
            @Parameter(description = "Meal payload", required = true)
            @Valid @RequestBody MealRequest request
    );

    @Operation(summary = "Delete meal", description = "Deletes a meal by ID.")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "204", description = "Meal deleted successfully"),
        @ApiResponse(
                responseCode = "404",
                description = "Meal not found",
                content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    })
    @DeleteMapping("/api/v1/meals/{id}")
    ResponseEntity<Void> delete(
            @Parameter(description = "Meal ID", required = true, example = "1")
            @PathVariable Long id
    );
}
