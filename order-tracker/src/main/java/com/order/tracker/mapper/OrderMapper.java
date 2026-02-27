package com.order.tracker.mapper;

import com.order.tracker.domain.Order;
import com.order.tracker.dto.OrderDto;
import org.springframework.stereotype.Component;

@Component
public class OrderMapper {

    public OrderDto toDto(final Order entity) {
        if (entity == null) {
            return null;
        }
        return new OrderDto(entity.getId(), entity.getAmount(), entity.getDate(), entity.getDescription());
    }

    public Order toDomain(final OrderDto dto) {
        if (dto == null) {
            return null;
        }
        return new Order(dto.getId(), dto.getAmount(), dto.getDate(), dto.getDescription());
    }
}
