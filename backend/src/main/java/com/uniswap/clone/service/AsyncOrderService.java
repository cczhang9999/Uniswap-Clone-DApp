package com.uniswap.clone.service;

import com.uniswap.clone.entity.Order;
import com.uniswap.clone.entity.OrderSummary;
import com.uniswap.clone.mapper.OrderSummaryMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

@Service
@Slf4j
@RequiredArgsConstructor
public class AsyncOrderService {

    private final OrderSummaryMapper orderSummaryMapper;

    @Async
    public void syncOrderToSummary(Order order) {
        try {
            log.info("Async syncing order {} to summary database...", order.getOrderId());
            OrderSummary summary = new OrderSummary();
            summary.setOrderId(order.getOrderId());
            summary.setUserId(order.getUserId());
            summary.setSymbol(order.getSymbol());
            summary.setSide(order.getSide());
            summary.setType(order.getType());
            summary.setPrice(order.getPrice());
            summary.setQuantity(order.getQuantity());
            summary.setStatus(order.getStatus());
            summary.setFilledQuantity(order.getFilledQuantity());
            summary.setTimestamp(order.getTimestamp());
            
            orderSummaryMapper.insert(summary);
            log.info("Async sync successful for order {}", order.getOrderId());
        } catch (Exception e) {
            log.error("Failed to sync order {} to summary database: {}", order.getOrderId(), e.getMessage());
            // Retry logic could be added here (e.g. send to Dead Letter Queue)
        }
    }
}
