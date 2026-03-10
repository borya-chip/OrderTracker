package com.order.tracker.service;

import com.order.tracker.dto.response.TransactionDemoResponse;

public interface TransactionDemoService {

    TransactionDemoResponse runWithoutTransactional();

    TransactionDemoResponse runWithTransactional();
}
