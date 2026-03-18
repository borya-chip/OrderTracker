package com.order.tracker.repository;

import com.order.tracker.domain.Restaurant;
import java.math.BigDecimal;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

@Repository
public interface RestaurantRepository extends JpaRepository<Restaurant, Long> {

    @Query(
            value = """
                    select distinct r
                    from Restaurant r
                    join r.meals m
                    join m.category c
                    where c.name = :categoryName
                      and m.price between :minMealPrice and :maxMealPrice
                    order by r.id
                    """)
    List<Restaurant> findRestaurantsByCategoryWithJpql(
            String categoryName, BigDecimal minMealPrice, BigDecimal maxMealPrice);

    @Query(
            value = """
                    select distinct r.*
                    from restaurants r
                    join meals m on m.restaurant_id = r.id
                    join categories c on c.id = m.category_id
                    where c.name = :categoryName
                      and m.price between :minMealPrice and :maxMealPrice
                    order by r.id
                    """,
            nativeQuery = true)
    List<Restaurant> findRestaurantsByCategoryWithNative(
            String categoryName, BigDecimal minMealPrice, BigDecimal maxMealPrice);
}
