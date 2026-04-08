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
import io.swagger.v3.oas.annotations.media.ExampleObject;
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
            @io.swagger.v3.oas.annotations.parameters.RequestBody(
                    required = true,
                    description = "Single order example. Replace customerId and mealIds with existing IDs from your database.",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = OrderRequest.class),
                            examples = @ExampleObject(
                                    name = "Single order demo",
                                    value = """
                                            {
                                              "amount": 25.00,
                                              "date": "2026-04-07T12:00:00",
                                              "description": "Single demo order",
                                              "customerId": 1,
                                              "mealIds": [1]
                                            }
                                            """)))
            @Valid @RequestBody OrderRequest request
    );

    @Operation(
            summary = "Bulk create orders",
            description = """
                    Imports a list of orders in transactional or non-transactional mode.
                    Demo flow in Swagger:
                    1. Create customer, category, restaurant and meal using the prepared examples.
                    2. Run this endpoint with transactional=true to show rollback of the whole batch.
                    3. Run the same body with transactional=false to show partial save without rollback.
                    4. Compare database state using GET /api/v1/orders before and after each call.
                    """
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
            @io.swagger.v3.oas.annotations.parameters.RequestBody(
                    required = true,
                    description = "Ready-made request body for demonstrating transactional vs non-transactional bulk import. Replace customerId and mealIds with real IDs from your DB.",
                    content = @Content(
                            mediaType = "application/json",
                            array = @ArraySchema(schema = @Schema(implementation = OrderRequest.class)),
                            examples = {
                                @ExampleObject(
                                        name = "Rollback demo body",
                                        summary = "Use with transactional=true",
                                        value = """
                                                [
                                                  {
                                                    "amount": 25.00,
                                                    "date": "2026-04-07T12:00:00",
                                                    "description": "TX valid order",
                                                    "customerId": 1,
                                                    "mealIds": [1]
                                                  },
                                                  {
                                                    "amount": 30.00,
                                                    "date": "2026-04-07T12:10:00",
                                                    "description": "TX invalid order",
                                                    "customerId": 1,
                                                    "mealIds": [1, 999999]
                                                  }
                                                ]
                                                """),
                                @ExampleObject(
                                        name = "Partial save demo body",
                                        summary = "Use with transactional=false",
                                        value = """
                                                [
                                                  {
                                                    "amount": 25.00,
                                                    "date": "2026-04-07T12:20:00",
                                                    "description": "NO_TX valid order",
                                                    "customerId": 1,
                                                    "mealIds": [1]
                                                  },
                                                  {
                                                    "amount": 30.00,
                                                    "date": "2026-04-07T12:30:00",
                                                    "description": "NO_TX invalid order",
                                                    "customerId": 1,
                                                    "mealIds": [1, 999999]
                                                  }
                                                ]
                                                """)
                            }))
            @Valid @RequestBody List<@Valid OrderRequest> requests,
            @Parameter(
                    description = "Run bulk import inside a transaction. true = rollback whole batch on error, false = successful items remain in DB.",
                    example = "true")
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
