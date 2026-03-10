package com.order.tracker.service.impl;

import com.order.tracker.domain.Order;
import com.order.tracker.dto.response.NPlusOneDemoResponse;
import com.order.tracker.mapper.OrderMapper;
import com.order.tracker.repository.OrderRepository;
import com.order.tracker.service.NPlusOneDemoService;
import jakarta.persistence.EntityManagerFactory;
import jakarta.persistence.PersistenceUnit;
import lombok.RequiredArgsConstructor;
import org.hibernate.SessionFactory;
import org.hibernate.stat.Statistics;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class NPlusOneDemoServiceImpl implements NPlusOneDemoService {

    private final OrderRepository orderRepository;
    private final OrderMapper orderMapper;

    @PersistenceUnit
    private EntityManagerFactory entityManagerFactory;

    @Override
    @Transactional(readOnly = true)
    public NPlusOneDemoResponse demonstrate() {
        Statistics statistics = entityManagerFactory.unwrap(SessionFactory.class).getStatistics();
        statistics.setStatisticsEnabled(true);

        statistics.clear();
        List<Order> orders = orderRepository.findAll();
        orders.stream().map(orderMapper::toResponse).toList();
        long queriesWithNPlusOne = statistics.getPrepareStatementCount();

        statistics.clear();
        List<Order> optimizedOrders = orderRepository.findAllWithDetails();
        optimizedOrders.stream().map(orderMapper::toResponse).toList();
        long queriesWithEntityGraph = statistics.getPrepareStatementCount();

        return new NPlusOneDemoResponse(
                orders.size(),
                queriesWithNPlusOne,
                queriesWithEntityGraph,
                Math.max(queriesWithNPlusOne - queriesWithEntityGraph, 0));
    }
}
