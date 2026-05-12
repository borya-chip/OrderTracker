package com.order.tracker.controller;

import com.order.tracker.controller.api.HealthControllerApi;
import com.order.tracker.dto.response.HealthResponse;
import com.order.tracker.service.HealthService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
public class HealthController implements HealthControllerApi {

    private static final String STATUS_UP = "UP";

    private final HealthService healthService;

    @Override
    @GetMapping("/api/v1/health")
    public ResponseEntity<HealthResponse> getHealth() {
        HealthResponse response = healthService.getHealth();
        HttpStatus httpStatus = STATUS_UP.equals(response.getStatus())
                ? HttpStatus.OK
                : HttpStatus.SERVICE_UNAVAILABLE;
        return ResponseEntity.status(httpStatus).body(response);
    }
}
