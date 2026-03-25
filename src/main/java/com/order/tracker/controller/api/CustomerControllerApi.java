package com.order.tracker.controller.api;

import com.order.tracker.dto.request.CustomerRequest;
import com.order.tracker.dto.response.CustomerResponse;
import com.order.tracker.exception.response.ErrorResponse;
import com.order.tracker.exception.response.ValidationErrorResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.ArraySchema;
import io.swagger.v3.oas.annotations.media.Content;
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

@Tag(name = "Customer Controller", description = "Customer management endpoints")
public interface CustomerControllerApi {

    @Operation(summary = "Get customer by ID", description = "Returns customer details by identifier.")
    @ApiResponses(value = {
        @ApiResponse(
                responseCode = "200",
                description = "Customer found",
                content = @Content(schema = @Schema(implementation = CustomerResponse.class))),
        @ApiResponse(
                responseCode = "404",
                description = "Customer not found",
                content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    })
    @GetMapping("/api/v1/customers/{id}")
    ResponseEntity<CustomerResponse> getById(
            @Parameter(description = "Customer ID", required = true, example = "1")
            @PathVariable Long id
    );

    @Operation(summary = "Get all customers", description = "Returns all customers.")
    @ApiResponses(value = {
        @ApiResponse(
                responseCode = "200",
                description = "Customers retrieved successfully",
                content = @Content(array = @ArraySchema(schema = @Schema(implementation = CustomerResponse.class))))
    })
    @GetMapping("/api/v1/customers")
    ResponseEntity<List<CustomerResponse>> getAll();

    @Operation(summary = "Create customer", description = "Creates a new customer.")
    @ApiResponses(value = {
        @ApiResponse(
                responseCode = "201",
                description = "Customer created successfully",
                content = @Content(schema = @Schema(implementation = CustomerResponse.class))),
        @ApiResponse(
                responseCode = "400",
                description = "Invalid request body",
                content = @Content(schema = @Schema(implementation = ValidationErrorResponse.class))),
        @ApiResponse(
                responseCode = "409",
                description = "Duplicate customer email",
                content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    })
    @PostMapping("/api/v1/customers")
    ResponseEntity<CustomerResponse> create(
            @Parameter(description = "Customer payload", required = true)
            @Valid @RequestBody CustomerRequest request
    );

    @Operation(summary = "Update customer", description = "Updates an existing customer.")
    @ApiResponses(value = {
        @ApiResponse(
                responseCode = "200",
                description = "Customer updated successfully",
                content = @Content(schema = @Schema(implementation = CustomerResponse.class))),
        @ApiResponse(
                responseCode = "400",
                description = "Invalid request body",
                content = @Content(schema = @Schema(implementation = ValidationErrorResponse.class))),
        @ApiResponse(
                responseCode = "404",
                description = "Customer not found",
                content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
        @ApiResponse(
                responseCode = "409",
                description = "Duplicate customer email",
                content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    })
    @PutMapping("/api/v1/customers/{id}")
    ResponseEntity<CustomerResponse> update(
            @Parameter(description = "Customer ID", required = true, example = "1")
            @PathVariable Long id,
            @Parameter(description = "Customer payload", required = true)
            @Valid @RequestBody CustomerRequest request
    );

    @Operation(summary = "Delete customer", description = "Deletes a customer by ID.")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "204", description = "Customer deleted successfully"),
        @ApiResponse(
                responseCode = "404",
                description = "Customer not found",
                content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    })
    @DeleteMapping("/api/v1/customers/{id}")
    ResponseEntity<Void> delete(
            @Parameter(description = "Customer ID", required = true, example = "1")
            @PathVariable Long id
    );
}
