package com.order.tracker.controller;

import com.order.tracker.controller.api.CustomerControllerApi;
import com.order.tracker.dto.request.CustomerRequest;
import com.order.tracker.dto.response.CustomerResponse;
import com.order.tracker.service.CustomerService;
import jakarta.validation.Valid;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
public class CustomerController implements CustomerControllerApi {

    private final CustomerService customerService;

    @PostMapping("/api/v1/customers")
    public ResponseEntity<CustomerResponse> create(@Valid @RequestBody final CustomerRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(customerService.create(request));
    }

    @GetMapping("/api/v1/customers/{id}")
    public ResponseEntity<CustomerResponse> getById(@PathVariable final Long id) {
        return ResponseEntity.ok(customerService.getById(id));
    }

    @GetMapping("/api/v1/customers")
    public ResponseEntity<List<CustomerResponse>> getAll() {
        return ResponseEntity.ok(customerService.getAll());
    }

    @PutMapping("/api/v1/customers/{id}")
    public ResponseEntity<CustomerResponse> update(
            @PathVariable final Long id,
            @Valid @RequestBody final CustomerRequest request) {
        return ResponseEntity.ok(customerService.update(id, request));
    }

    @DeleteMapping("/api/v1/customers/{id}")
    public ResponseEntity<Void> delete(@PathVariable final Long id) {
        customerService.delete(id);
        return ResponseEntity.noContent().build();
    }
}
