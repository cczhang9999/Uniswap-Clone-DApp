package com.uniswap.clone.service;

import com.uniswap.clone.entity.Order;
import com.uniswap.clone.mapper.OrderMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.CachePut;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;

@Service
@Slf4j
@RequiredArgsConstructor
public class OrderCacheService {

    private final OrderMapper orderMapper;

    @Cacheable(value = "order", key = "#orderId", unless = "#result == null")
    public Order getOrder(String orderId) {
        // Cache Aside: If not in cache, finding here will put it in cache due to @Cacheable
        // Note: ShardingSphere handles the DB routing
        com.baomidou.mybatisplus.core.conditions.query.QueryWrapper<Order> query = new com.baomidou.mybatisplus.core.conditions.query.QueryWrapper<>();
        query.eq("order_id", orderId);
        return orderMapper.selectOne(query);
    }

    @CachePut(value = "order", key = "#order.orderId")
    public Order cacheOrder(Order order) {
        log.info("Caching order: {}", order.getOrderId());
        return order;
    }
}
