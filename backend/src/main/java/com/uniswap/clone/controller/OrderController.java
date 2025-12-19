package com.uniswap.clone.controller;

import com.uniswap.clone.engine.MatchingEngine;
import com.uniswap.clone.engine.OrderBook;
import com.uniswap.clone.model.Order;
import com.uniswap.clone.model.Trade;
import com.uniswap.clone.model.Wallet;
import com.uniswap.clone.service.ClearingService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1")
@CrossOrigin(origins = "http://localhost:3000")
@RequiredArgsConstructor
public class OrderController {

    private final MatchingEngine matchingEngine;
    private final ClearingService clearingService;
    private final com.uniswap.clone.mapper.OrderMapper orderMapper;
    private final com.uniswap.clone.service.OrderService orderService;
    private final com.uniswap.clone.mapper.OrderSummaryMapper orderSummaryMapper;

    @GetMapping("/orders/summary")
    public com.uniswap.clone.common.Result<List<com.uniswap.clone.entity.OrderSummary>> getAllOrdersSummary() {
        com.baomidou.mybatisplus.core.conditions.query.QueryWrapper<com.uniswap.clone.entity.OrderSummary> query = new com.baomidou.mybatisplus.core.conditions.query.QueryWrapper<>();
        query.orderByDesc("timestamp").last("LIMIT 50");
        return com.uniswap.clone.common.Result.success(orderSummaryMapper.selectList(query));
    }

    @PostMapping("/order")
    public com.uniswap.clone.common.Result<List<Trade>> placeOrder(@RequestBody OrderRequest request) {
        return orderService.createOrder(
            request.getUserId(),
            request.getSymbol(),
            request.getSide().toString(),
            request.getType().toString(),
            request.getPrice(),
            request.getQuantity()
        );
    }

    @DeleteMapping("/order/{orderId}")
    public com.uniswap.clone.common.Result<String> cancelOrder(@PathVariable String orderId, @RequestParam String userId) {
        return orderService.cancelOrder(orderId, userId);
    }

    @GetMapping("/order/book/{symbol}")
    public com.uniswap.clone.common.Result<OrderBookDTO> getOrderBook(@PathVariable String symbol) {
        OrderBook orderBook = matchingEngine.getOrderBook(symbol);
        System.out.println("Order Book: " + orderBook);
        return com.uniswap.clone.common.Result.success(new OrderBookDTO(orderBook.getBids(), orderBook.getAsks()));
    }
    
    @GetMapping("/balance/{userId}")
    public com.uniswap.clone.common.Result<Wallet> getBalance(@PathVariable String userId) {
        return com.uniswap.clone.common.Result.success(clearingService.getWallet(userId));
    }
    
    @PostMapping("/balance/deposit")
    public com.uniswap.clone.common.Result<String> deposit(@RequestBody DepositRequest request) {
            clearingService.deposit(request.getUserId(), request.getCurrency(), request.getAmount());
            return com.uniswap.clone.common.Result.success("Deposit successful");
    }

    @GetMapping("/orders/{userId}")
    public com.uniswap.clone.common.Result<List<com.uniswap.clone.entity.Order>> getOrders(@PathVariable String userId) {
        com.baomidou.mybatisplus.core.conditions.query.QueryWrapper<com.uniswap.clone.entity.Order> query = new com.baomidou.mybatisplus.core.conditions.query.QueryWrapper<>();
        query.eq("user_id", userId).orderByDesc("timestamp");
        return com.uniswap.clone.common.Result.success(orderMapper.selectList(query));
    }
    
    @GetMapping("/trades/{userId}")
    public com.uniswap.clone.common.Result<List<com.uniswap.clone.entity.Trade>> getTrades(@PathVariable String userId) {
        return com.uniswap.clone.common.Result.success(clearingService.getUserTrades(userId));
    }
    
    static class DepositRequest {
        private String userId;
        private String currency;
        private BigDecimal amount;
        
        public String getUserId() { return userId; }
        public void setUserId(String userId) { this.userId = userId; }
        public String getCurrency() { return currency; }
        public void setCurrency(String currency) { this.currency = currency; }
        public BigDecimal getAmount() { return amount; }
        public void setAmount(BigDecimal amount) { this.amount = amount; }
    }
    
    static class OrderBookDTO {
        private final List<Order> bids;
        private final List<Order> asks;

        public OrderBookDTO(List<Order> bids, List<Order> asks) {
            this.bids = bids;
            this.asks = asks;
        }

        public List<Order> getBids() { return bids; }
        public List<Order> getAsks() { return asks; }
    }
}
