package com.order.tracker.repository;

import java.time.LocalDate;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

import com.order.tracker.domain.Order;

public interface OrderRepository extends JpaRepository<Order, Long> {

    List<Order> findByDateBetween(LocalDate startDate, LocalDate endDate);

    List<Order> findByDateGreaterThanEqual(LocalDate startDate);

    List<Order> findByDateLessThanEqual(LocalDate endDate);
}
