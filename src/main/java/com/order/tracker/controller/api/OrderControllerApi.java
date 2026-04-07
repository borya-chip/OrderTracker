package com.order.tracker.controller.api;

import com.order.tracker.dto.request.OrderRequest;
import com.order.tracker.dto.request.OrderUpdateRequest;
import com.order.tracker.dto.response.OrderResponse;
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
import java.time.LocalDate;
import java.util.List;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;

@Tag(name = "Order Controller", description = "Order management endpoints")
public interface OrderControllerApi {

    @Operation(summary = "Get order by ID", description = "Returns order details by identifier.")
    @ApiResponses(value = {
        @ApiResponse(
                responseCode = "200",
                description = "Order found",
                content = @Content(schema = @Schema(implementation = OrderResponse.class))),
        @ApiResponse(
                responseCode = "404",
                description = "Order not found",
                content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    })
    @GetMapping("/api/v1/orders/{id}")
    ResponseEntity<OrderResponse> getById(
            @Parameter(description = "Order ID", required = true, example = "1")
            @PathVariable Long id
    );

    @Operation(
            summary = "Get orders",
            description = "Returns all orders or filters them by date range."
    )
    @ApiResponses(value = {
        @ApiResponse(
                responseCode = "200",
                description = "Orders retrieved successfully",
                content = @Content(array = @ArraySchema(schema = @Schema(implementation = OrderResponse.class)))),
        @ApiResponse(
                responseCode = "400",
                description = "Invalid query parameters",
                content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    })
    @GetMapping("/api/v1/orders")
    ResponseEntity<List<OrderResponse>> getByDateRange(
            @Parameter(description = "Start date filter", example = "2026-03-01")
            @RequestParam(required = false) LocalDate startDate,
            @Parameter(description = "End date filter", example = "2026-03-31")
            @RequestParam(required = false) LocalDate endDate,
            @Parameter(description = "Whether to fetch orders with EntityGraph", example = "false")
            @RequestParam(required = false, defaultValue = "false") boolean withEntityGraph
    );

    @Operation(summary = "Create order", description = "Creates a new order.")
    @ApiResponses(value = {
        @ApiResponse(
                responseCode = "201",
                description = "Order created successfully",
                content = @Content(schema = @Schema(implementation = OrderResponse.class))),
        @ApiResponse(
                responseCode = "400",
                description = "Invalid request body",
                content = @Content(schema = @Schema(implementation = ValidationErrorResponse.class))),
        @ApiResponse(
                responseCode = "404",
                description = "Customer or meals not found",
                content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    })
    @PostMapping("/api/v1/orders")
    ResponseEntity<OrderResponse> createOrder(
            @Parameter(description = "Order payload", required = true)
            @Valid @RequestBody OrderRequest request
    );

    @Operation(
            summary = "Bulk create orders",
            description = "Imports a list of orders in transactional or non-transactional mode."
    )
    @ApiResponses(value = {
        @ApiResponse(
                responseCode = "201",
                description = "Orders created successfully",
                content = @Content(array = @ArraySchema(schema = @Schema(implementation = OrderResponse.class)))),
        @ApiResponse(
                responseCode = "400",
                description = "Invalid request body",
                content = @Content(schema = @Schema(implementation = ValidationErrorResponse.class))),
        @ApiResponse(
                responseCode = "404",
                description = "Customer or meals not found",
                content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    })
    @PostMapping("/api/v1/orders/bulk")
    ResponseEntity<List<OrderResponse>> createOrdersBulk(
            @Parameter(description = "List of order payloads", required = true)
            @Valid @RequestBody List<@Valid OrderRequest> requests,
            @Parameter(description = "Run bulk import inside a transaction", example = "true")
            @RequestParam(defaultValue = "true") boolean transactional
    );

    @Operation(summary = "Patch order", description = "Partially updates an order.")
    @ApiResponses(value = {
        @ApiResponse(
                responseCode = "200",
                description = "Order updated successfully",
                content = @Content(schema = @Schema(implementation = OrderResponse.class))),
        @ApiResponse(
                responseCode = "400",
                description = "Invalid request body",
                content = @Content(schema = @Schema(implementation = ValidationErrorResponse.class))),
        @ApiResponse(
                responseCode = "404",
                description = "Order, customer or meals not found",
                content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    })
    @PatchMapping("/api/v1/orders/{id}")
    ResponseEntity<OrderResponse> updateOrder(
            @Parameter(description = "Order ID", required = true, example = "1")
            @PathVariable Long id,
            @Parameter(description = "Partial order payload", required = true)
            @Valid @RequestBody OrderUpdateRequest request
    );

    @Operation(summary = "Delete order", description = "Deletes an order by ID.")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "204", description = "Order deleted successfully"),
        @ApiResponse(
                responseCode = "404",
                description = "Order not found",
                content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    })
    @DeleteMapping("/api/v1/orders/{id}")
    ResponseEntity<Void> deleteOrder(
            @Parameter(description = "Order ID", required = true, example = "1")
            @PathVariable Long id
    );
}
