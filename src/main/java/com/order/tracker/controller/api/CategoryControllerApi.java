package com.order.tracker.controller.api;

import com.order.tracker.dto.request.CategoryRequest;
import com.order.tracker.dto.response.CategoryResponse;
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
import java.util.List;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;

@Tag(name = "Category Controller", description = "Category management endpoints")
public interface CategoryControllerApi {

    @Operation(summary = "Get category by ID", description = "Returns category details by identifier.")
    @ApiResponses(value = {
        @ApiResponse(
                responseCode = "200",
                description = "Category found",
                content = @Content(schema = @Schema(implementation = CategoryResponse.class))),
        @ApiResponse(
                responseCode = "404",
                description = "Category not found",
                content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    })
    @GetMapping("/api/v1/categories/{id}")
    ResponseEntity<CategoryResponse> getById(
            @Parameter(description = "Category ID", required = true, example = "1")
            @PathVariable Long id
    );

    @Operation(summary = "Get all categories", description = "Returns all categories.")
    @ApiResponses(value = {
        @ApiResponse(
                responseCode = "200",
                description = "Categories retrieved successfully",
                content = @Content(array = @ArraySchema(schema = @Schema(implementation = CategoryResponse.class))))
    })
    @GetMapping("/api/v1/categories")
    ResponseEntity<List<CategoryResponse>> getAll();

    @Operation(summary = "Create category", description = "Creates a new category.")
    @ApiResponses(value = {
        @ApiResponse(
                responseCode = "201",
                description = "Category created successfully",
                content = @Content(schema = @Schema(implementation = CategoryResponse.class))),
        @ApiResponse(
                responseCode = "400",
                description = "Invalid request body",
                content = @Content(schema = @Schema(implementation = ValidationErrorResponse.class)))
    })
    @PostMapping("/api/v1/categories")
    ResponseEntity<CategoryResponse> create(
            @Parameter(description = "Category payload", required = true)
            @io.swagger.v3.oas.annotations.parameters.RequestBody(
                    required = true,
                    description = "Demo category for transactional bulk import scenario.",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = CategoryRequest.class),
                            examples = @ExampleObject(
                                    name = "Transactional demo category",
                                    value = """
                                            {
                                              "name": "Transactional Demo Category"
                                            }
                                            """)))
            @Valid @RequestBody CategoryRequest request
    );

    @Operation(summary = "Update category", description = "Updates an existing category.")
    @ApiResponses(value = {
        @ApiResponse(
                responseCode = "200",
                description = "Category updated successfully",
                content = @Content(schema = @Schema(implementation = CategoryResponse.class))),
        @ApiResponse(
                responseCode = "400",
                description = "Invalid request body",
                content = @Content(schema = @Schema(implementation = ValidationErrorResponse.class))),
        @ApiResponse(
                responseCode = "404",
                description = "Category not found",
                content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    })
    @PutMapping("/api/v1/categories/{id}")
    ResponseEntity<CategoryResponse> update(
            @Parameter(description = "Category ID", required = true, example = "1")
            @PathVariable Long id,
            @Parameter(description = "Category payload", required = true)
            @Valid @RequestBody CategoryRequest request
    );

    @Operation(summary = "Delete category", description = "Deletes a category by ID.")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "204", description = "Category deleted successfully"),
        @ApiResponse(
                responseCode = "404",
                description = "Category not found",
                content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    })
    @DeleteMapping("/api/v1/categories/{id}")
    ResponseEntity<Void> delete(
            @Parameter(description = "Category ID", required = true, example = "1")
            @PathVariable Long id
    );
}
