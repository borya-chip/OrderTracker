package com.order.tracker.repository;

import java.time.LocalDateTime;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

import com.order.tracker.domain.Order;

public interface OrderRepository extends JpaRepository<Order, Long> {

    List<Order> findByDateBetween(LocalDateTime startDate, LocalDateTime endDate);

    List<Order> findByDateGreaterThanEqual(LocalDateTime startDate);

    List<Order> findByDateLessThanEqual(LocalDateTime endDate);
}
