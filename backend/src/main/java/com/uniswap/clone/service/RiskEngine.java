package com.uniswap.clone.service;

import com.uniswap.clone.model.Order;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;

@Service
public class RiskEngine {

    private static final BigDecimal MAX_ORDER_SIZE = new BigDecimal("1000");
    private static final BigDecimal MIN_ORDER_SIZE = new BigDecimal("0.0001");

    /**
     * Pre-trade risk checks.
     * @param order The order to check.
     * @return true if passed, throws RuntimeException if failed.
     */
    public boolean preTradeCheck(Order order) {
        // 1. Check Min Order Size
        if (order.getQuantity().compareTo(MIN_ORDER_SIZE) < 0) {
            throw new RuntimeException("Order quantity too small. Min: " + MIN_ORDER_SIZE);
        }

        // 2. Check Max Order Size
        if (order.getQuantity().compareTo(MAX_ORDER_SIZE) > 0) {
            throw new RuntimeException("Order quantity too large. Max: " + MAX_ORDER_SIZE);
        }

        // 3. Price Check (Simple: Price must be positive)
        if (order.getPrice().compareTo(BigDecimal.ZERO) <= 0) {
            throw new RuntimeException("Price must be positive.");
        }

        return true;
    }
}
