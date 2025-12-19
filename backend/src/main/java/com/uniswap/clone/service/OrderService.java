package com.uniswap.clone.service;

import com.uniswap.clone.common.Result;
import com.uniswap.clone.engine.MatchingEngine;
import com.uniswap.clone.entity.Order;
import com.uniswap.clone.mapper.OrderMapper;
import com.uniswap.clone.model.Trade;
import io.seata.spring.annotation.GlobalTransactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

@Service
@Slf4j
@RequiredArgsConstructor
public class OrderService {

    private final MatchingEngine matchingEngine;
    private final OrderMapper orderMapper;
    private final OrderCacheService orderCacheService;
    private final com.uniswap.clone.service.ClearingService clearingService;
    private final AsyncOrderService asyncOrderService;

    @GlobalTransactional(name = "create-order-tx", rollbackFor = Exception.class)
    @com.alibaba.csp.sentinel.annotation.SentinelResource(value = "createOrder", fallbackClass = com.uniswap.clone.fallback.OrderFallback.class, fallback = "createOrderFallback")
    public Result<List<Trade>> createOrder(String userId, String symbol, String side, String type, BigDecimal price, BigDecimal quantity) {
        String orderId = UUID.randomUUID().toString();

        // 1. Create Order Entity
        Order orderEntity = new Order();
        orderEntity.setOrderId(orderId);
        orderEntity.setUserId(userId);
        orderEntity.setSymbol(symbol);
        orderEntity.setSide(side);
        orderEntity.setType(type);
        orderEntity.setPrice(price);
        orderEntity.setQuantity(quantity);
        orderEntity.setStatus("PENDING");
        orderEntity.setFilledQuantity(BigDecimal.ZERO);
        orderEntity.setTimestamp(System.currentTimeMillis());

        // 2. Persist to DB (Sharded)
        orderMapper.insert(orderEntity);
        
        // 3. Cache Order
        orderCacheService.cacheOrder(orderEntity);

        // 3.1 Async Sync to Summary DB
        asyncOrderService.syncOrderToSummary(orderEntity);

        // 4. Process Order in Matching Engine (In-memory for this node)
        // Note: In a real distributed system, this might be an RPC call or MQ message
        com.uniswap.clone.model.Order engineOrder = new com.uniswap.clone.model.Order(
                userId,
                orderId,
                symbol,
                com.uniswap.clone.model.Order.Side.valueOf(side),
                com.uniswap.clone.model.Order.Type.valueOf(type),
                price,
                quantity,
                orderEntity.getTimestamp()
        );
        
        List<Trade> trades = matchingEngine.processOrder(engineOrder);
        
        return Result.success(trades);
    }

    @GlobalTransactional(rollbackFor = Exception.class)
    public Result<String> cancelOrder(String orderId, String userId) {
        // 1. Get Order
        com.baomidou.mybatisplus.core.conditions.query.QueryWrapper<Order> query = new com.baomidou.mybatisplus.core.conditions.query.QueryWrapper<>();
        query.eq("order_id", orderId);
        query.eq("user_id", userId);
        Order order = orderMapper.selectOne(query);

        if (order == null) {
            return Result.error(404, "Order not found");
        }

        if (!"PENDING".equals(order.getStatus())) {
            return Result.error(400, "Order status is not PENDING");
        }

        // 2. Update Status to CANCELLED
        order.setStatus("CANCELLED");
        orderMapper.updateById(order);

        // 3. Remove from Matching Engine (Memory)
        com.uniswap.clone.model.Order engineOrder = new com.uniswap.clone.model.Order(
                userId,
                orderId,
                order.getSymbol(),
                com.uniswap.clone.model.Order.Side.valueOf(order.getSide()),
                com.uniswap.clone.model.Order.Type.valueOf(order.getType()),
                order.getPrice(),
                order.getQuantity(),
                order.getTimestamp()
        );
        
        boolean removed = matchingEngine.cancelOrder(engineOrder);
        if (!removed) {
            // Note: It might be partially filled or already matched in race condition.
            // For strict consistency, we might fail here. 
            // Or if it's missing from book but status was PENDING, it's a data anomaly.
            // Let's assume if it wasn't in the book, it might have just been matched.
            log.warn("Order {} not found in matching engine during cancellation", orderId);
            // Verify if it was just traded? (Skipped for simplicity, assuming removal failure means mismatch)
             throw new RuntimeException("Failed to remove order from matching engine");
        }

        // 4. Unfreeze Funds
        String[] parts = order.getSymbol().split("-");
        String baseCurrency = parts[0];
        String quoteCurrency = parts[1];
        
        String currencyToUnfreeze;
        BigDecimal amountToUnfreeze;

        if ("BUY".equals(order.getSide())) {
            currencyToUnfreeze = quoteCurrency;
            // Original frozen was Price * Quantity. 
            // (If partial fill supported, we'd need remaining qty. Here assuming simple PENDING->CANCEL full)
            amountToUnfreeze = order.getPrice().multiply(order.getQuantity());
        } else {
            currencyToUnfreeze = baseCurrency;
            amountToUnfreeze = order.getQuantity();
        }

        clearingService.unfreezeFunds(userId, currencyToUnfreeze, amountToUnfreeze);

        return Result.success("Order cancelled");
    }

}

