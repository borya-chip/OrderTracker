package com.order.tracker.controller;

import com.order.tracker.dto.response.NPlusOneDemoResponse;
import com.order.tracker.dto.response.TransactionDemoResponse;
import com.order.tracker.service.NPlusOneDemoService;
import com.order.tracker.service.TransactionDemoService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/demo")
public class DemoController {

    private final NPlusOneDemoService nPlusOneDemoService;
    private final TransactionDemoService transactionDemoService;

    @GetMapping("/n-plus-one")
    public ResponseEntity<NPlusOneDemoResponse> nPlusOneDemo() {
        return ResponseEntity.ok(nPlusOneDemoService.demonstrate());
    }

    @PostMapping("/transaction/without")
    public ResponseEntity<TransactionDemoResponse> withoutTransaction() {
        return ResponseEntity.ok(transactionDemoService.runWithoutTransactional());
    }

    @PostMapping("/transaction/with")
    public ResponseEntity<TransactionDemoResponse> withTransaction() {
        return ResponseEntity.ok(transactionDemoService.runWithTransactional());
    }
}
