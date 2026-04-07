package com.order.tracker.repository;

import com.order.tracker.domain.Order;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface OrderRepository extends JpaRepository<Order, Long> {

    List<Order> findByDateBetween(LocalDateTime startDate, LocalDateTime endDate);

    @Query("SELECT o FROM Order o")
    List<Order> findAllOrders();

    @EntityGraph(attributePaths = { "customer", "meals", "meals.category", "meals.restaurant" })
    @Query("SELECT o FROM Order o")
    List<Order> findAllOrdersWithEntityGraph();

    @EntityGraph(attributePaths = { "customer", "meals", "meals.category", "meals.restaurant" })
    @Query("SELECT o FROM Order o WHERE o.id = :id")
    Optional<Order> findByIdWithDetails(@Param("id") Long id);
}
