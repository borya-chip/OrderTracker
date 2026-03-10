package com.order.tracker.service;

import com.order.tracker.dto.request.CustomerRequest;
import com.order.tracker.dto.response.CustomerResponse;

import java.util.List;

public interface CustomerService {

    CustomerResponse create(CustomerRequest request);

    CustomerResponse getById(Long id);

    List<CustomerResponse> getAll();

    CustomerResponse update(Long id, CustomerRequest request);

    void delete(Long id);
}
