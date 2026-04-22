package com.order.tracker.service.impl;

import com.order.tracker.domain.Customer;
import com.order.tracker.dto.request.CustomerRequest;
import com.order.tracker.dto.response.CustomerResponse;
import com.order.tracker.exception.DuplicateResourceException;
import com.order.tracker.exception.ResourceNotFoundException;
import com.order.tracker.mapper.CustomerMapper;
import com.order.tracker.repository.CustomerRepository;
import com.order.tracker.service.CustomerService;
import lombok.RequiredArgsConstructor;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
public class CustomerServiceImpl implements CustomerService {

    private final CustomerRepository customerRepository;
    private final CustomerMapper customerMapper;

    @Override
    @Transactional
    public CustomerResponse create(final CustomerRequest request) {
        Customer customer = new Customer();
        apply(customer, request);
        return customerMapper.toResponse(saveCustomer(customer));
    }

    @Override
    public CustomerResponse getById(final Long id) {
        return customerMapper.toResponse(findCustomer(id));
    }

    @Override
    public List<CustomerResponse> getAll() {
        return customerRepository.findAll().stream()
                .map(customerMapper::toResponse)
                .toList();
    }

    @Override
    @Transactional
    public CustomerResponse update(final Long id, final CustomerRequest request) {
        Customer customer = findCustomer(id);
        apply(customer, request);
        return customerMapper.toResponse(saveCustomer(customer));
    }

    @Override
    @Transactional
    public void delete(final Long id) {
        if (!customerRepository.existsById(id)) {
            throw new ResourceNotFoundException("Customer not found: " + id);
        }
        customerRepository.deleteById(id);
    }

    private Customer findCustomer(final Long id) {
        return customerRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Customer not found: " + id));
    }

    private void apply(final Customer customer, final CustomerRequest request) {
        customer.setFirstName(request.getFirstName());
        customer.setLastName(request.getLastName());
        customer.setEmail(request.getEmail());
        customer.setPhoneNumber(request.getPhoneNumber());
    }

    private Customer saveCustomer(final Customer customer) {
        try {
            return customerRepository.saveAndFlush(customer);
        } catch (DataIntegrityViolationException exception) {
            throw new DuplicateResourceException(
                    "Customer with email '%s' already exists".formatted(customer.getEmail()));
        }
    }
}
