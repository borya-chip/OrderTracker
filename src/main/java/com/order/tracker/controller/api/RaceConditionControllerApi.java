package com.order.tracker.controller.api;

import com.order.tracker.dto.response.RaceConditionDemoResponse;
import com.order.tracker.exception.response.ErrorResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;

@Tag(name = "Race Condition Controller", description = "Race condition demo endpoints")
public interface RaceConditionControllerApi {

    @Operation(
            summary = "Run race condition demo",
            description = """
                    Runs concurrent increments with unsafe and atomic counters
                    using ExecutorService with 50 threads.
                    """
    )
    @ApiResponses(value = {
        @ApiResponse(
                responseCode = "200",
                description = "Demo completed successfully",
                content = @Content(schema = @Schema(implementation = RaceConditionDemoResponse.class))),
        @ApiResponse(
                responseCode = "500",
                description = "Demo execution failed",
                content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    })
    @GetMapping("/api/v1/demo/race-condition/run")
    ResponseEntity<RaceConditionDemoResponse> runRaceConditionDemo() throws InterruptedException;
}
