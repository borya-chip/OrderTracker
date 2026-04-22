package com.order.tracker.controller.api;

import com.order.tracker.domain.AsyncTask;
import com.order.tracker.dto.request.OrderRequest;
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
import java.util.Map;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;

@Tag(name = "Async Order Controller", description = "Async order import endpoints")
public interface AsyncOrderControllerApi {

    @Operation(
            summary = "Create orders asynchronously",
            description = """
                    Starts async bulk order creation and returns task identifier immediately.
                    Use the returned taskId with the status endpoint to track progress.
                    """
    )
    @ApiResponses(value = {
        @ApiResponse(
                responseCode = "202",
                description = "Async import started",
                content = @Content(
                        mediaType = "application/json",
                        examples = @ExampleObject(value = """
                                {
                                  "taskId": "d6b62456-69ba-4e23-aa77-5747bc9292ce"
                                }
                                """))),
        @ApiResponse(
                responseCode = "400",
                description = "Invalid request body",
                content = @Content(schema = @Schema(implementation = ValidationErrorResponse.class)))
    })
    @PostMapping("/api/v1/orders/async")
    ResponseEntity<Map<String, String>> createOrdersAsync(
            @Parameter(description = "List of order payloads", required = true)
            @io.swagger.v3.oas.annotations.parameters.RequestBody(
                    required = true,
                    content = @Content(
                            mediaType = "application/json",
                            array = @ArraySchema(schema = @Schema(implementation = OrderRequest.class)),
                            examples = @ExampleObject(
                                    name = "Async import body",
                                    value = """
                                            [
                                              {
                                                "amount": 25.00,
                                                "date": "2026-04-07T12:00:00",
                                                "description": "Async order 1",
                                                "customerId": 1,
                                                "mealIds": [1]
                                              },
                                              {
                                                "amount": 30.00,
                                                "date": "2026-04-07T12:10:00",
                                                "description": "Async order 2",
                                                "customerId": 1,
                                                "mealIds": [1]
                                              }
                                            ]
                                            """)))
            @Valid @RequestBody List<@Valid OrderRequest> requests,
            @Parameter(
                    description = """
                            true = one transaction for the whole batch,
                            false = each order is saved independently.
                            """,
                    example = "true")
            @RequestParam(defaultValue = "true") boolean transactional
    );

    @Operation(summary = "Get async task status", description = "Returns status for a previously created task.")
    @ApiResponses(value = {
        @ApiResponse(
                responseCode = "200",
                description = "Task found",
                content = @Content(schema = @Schema(implementation = AsyncTask.class))),
        @ApiResponse(
                responseCode = "404",
                description = "Task not found",
                content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    })
    @GetMapping("/api/v1/orders/async/status/{taskId}")
    ResponseEntity<AsyncTask> getTaskStatus(
            @Parameter(description = "Async task ID", required = true)
            @PathVariable String taskId
    );

    @Operation(summary = "Get all async tasks", description = "Returns all async tasks stored in memory.")
    @ApiResponse(
            responseCode = "200",
            description = "Tasks retrieved successfully",
            content = @Content(schema = @Schema(implementation = AsyncTask.class)))
    @GetMapping("/api/v1/orders/async/tasks")
    ResponseEntity<Map<String, AsyncTask>> getAllAsyncTasks();
}
