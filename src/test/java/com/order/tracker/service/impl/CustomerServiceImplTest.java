package com.order.tracker.service.impl;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.order.tracker.domain.Customer;
import com.order.tracker.dto.request.CustomerRequest;
import com.order.tracker.exception.ResourceNotFoundException;
import com.order.tracker.mapper.CustomerMapper;
import com.order.tracker.repository.CustomerRepository;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class CustomerServiceImplTest {

    @Mock
    private CustomerRepository customerRepository;

    private CustomerServiceImpl service;

    @BeforeEach
    void setUp() {
        service = new CustomerServiceImpl(customerRepository, new CustomerMapper());
    }

    @Test
    void getByIdShouldReturnMappedCustomer() {
        when(customerRepository.findById(1L)).thenReturn(Optional.of(customer(1L, "Alex", "Brown")));

        var response = service.getById(1L);

        assertEquals("Alex", response.getFirstName());
        assertEquals("Brown", response.getLastName());
    }

    @Test
    void getByIdShouldThrowWhenMissing() {
        when(customerRepository.findById(1L)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> service.getById(1L));
    }

    @Test
    void getAllShouldReturnMappedCustomers() {
        when(customerRepository.findAll()).thenReturn(List.of(customer(1L, "Alex", "Brown")));

        var response = service.getAll();

        assertEquals(1, response.size());
        assertEquals(1L, response.get(0).getId());
    }

    @Test
    void createShouldSaveCustomer() {
        CustomerRequest request = new CustomerRequest("Alex", "Brown", "alex@example.com", "+123");
        when(customerRepository.save(any(Customer.class))).thenAnswer(invocation -> {
            Customer customer = invocation.getArgument(0);
            customer.setId(8L);
            return customer;
        });

        var response = service.create(request);

        assertEquals(8L, response.getId());
        assertEquals("alex@example.com", response.getEmail());
    }

    @Test
    void updateShouldSaveUpdatedCustomer() {
        Customer existing = customer(5L, "Alex", "Brown");
        when(customerRepository.findById(5L)).thenReturn(Optional.of(existing));
        when(customerRepository.save(any(Customer.class))).thenAnswer(invocation -> invocation.getArgument(0));

        var response = service.update(5L, new CustomerRequest("Sam", "Green", "sam@example.com", "+999"));

        assertEquals("Sam", response.getFirstName());
        assertEquals("sam@example.com", response.getEmail());
    }

    @Test
    void deleteShouldDeleteExistingCustomer() {
        when(customerRepository.existsById(4L)).thenReturn(true);

        service.delete(4L);

        verify(customerRepository).deleteById(4L);
    }

    @Test
    void deleteShouldThrowWhenMissing() {
        when(customerRepository.existsById(4L)).thenReturn(false);

        assertThrows(ResourceNotFoundException.class, () -> service.delete(4L));

        verify(customerRepository, never()).deleteById(any());
    }

    private static Customer customer(final Long id, final String firstName, final String lastName) {
        Customer customer = new Customer();
        customer.setId(id);
        customer.setFirstName(firstName);
        customer.setLastName(lastName);
        customer.setEmail(firstName.toLowerCase() + "@example.com");
        customer.setPhoneNumber("+123");
        return customer;
    }
}
