package com.order.tracker.service;

import com.order.tracker.dto.OrderDto;

import java.time.LocalDate;
import java.util.List;

public interface OrderService {

    OrderDto getById(Long id);

    List<OrderDto> getByDateRange(final LocalDate startDate, final LocalDate endDate);
}
