package com.order.tracker.repository;

import com.order.tracker.domain.Order;
import org.springframework.data.jpa.repository.EntityGraph;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface OrderRepository extends JpaRepository<Order, Long> {

    List<Order> findByDateBetween(LocalDateTime startDate, LocalDateTime endDate);

    List<Order> findByDateGreaterThanEqual(LocalDateTime startDate);

    List<Order> findByDateLessThanEqual(LocalDateTime endDate);

    @EntityGraph(attributePaths = { "customer", "meals", "meals.category", "meals.restaurant" })
    @Query("select o from Order o")
    List<Order> findAllWithDetails();

    @EntityGraph(attributePaths = { "customer", "meals", "meals.category", "meals.restaurant" })
    @Query("select o from Order o where o.id = :id")
    Optional<Order> findByIdWithDetails(@Param("id") Long id);

    @EntityGraph(attributePaths = { "customer", "meals", "meals.category", "meals.restaurant" })
    @Query("select o from Order o where o.date between :startDate and :endDate")
    List<Order> findWithDetailsByDateBetween(
            @Param("startDate") LocalDateTime startDate,
            @Param("endDate") LocalDateTime endDate);

    @EntityGraph(attributePaths = { "customer", "meals", "meals.category", "meals.restaurant" })
    @Query("select o from Order o where o.date >= :startDate")
    List<Order> findWithDetailsByDateGreaterThanEqual(@Param("startDate") LocalDateTime startDate);

    @EntityGraph(attributePaths = { "customer", "meals", "meals.category", "meals.restaurant" })
    @Query("select o from Order o where o.date <= :endDate")
    List<Order> findWithDetailsByDateLessThanEqual(@Param("endDate") LocalDateTime endDate);
}
