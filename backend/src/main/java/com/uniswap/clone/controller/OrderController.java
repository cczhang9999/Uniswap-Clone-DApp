package com.uniswap.clone.controller;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.uniswap.clone.common.Result;
import com.uniswap.clone.engine.MatchingEngine;
import com.uniswap.clone.engine.OrderBook;
import com.uniswap.clone.entity.OrderSummary;
import com.uniswap.clone.mapper.OrderMapper;
import com.uniswap.clone.mapper.OrderSummaryMapper;
import com.uniswap.clone.model.Order;
import com.uniswap.clone.model.Trade;
import com.uniswap.clone.model.Wallet;
import com.uniswap.clone.service.ClearingService;
import com.uniswap.clone.service.OrderService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.List;


@RestController
@RequestMapping("/api/v1")
@CrossOrigin(origins = "http://localhost:3000")
@RequiredArgsConstructor
public class OrderController {

    private final MatchingEngine matchingEngine;
    private final ClearingService clearingService;
    private final OrderMapper orderMapper;
    private final OrderService orderService;
    private final OrderSummaryMapper orderSummaryMapper;

    @GetMapping("/orders/summary")
    public Result<List<OrderSummary>> getAllOrdersSummary() {
        QueryWrapper<OrderSummary> query = new QueryWrapper<>();
        query.orderByDesc("timestamp").last("LIMIT 50");
        return Result.success(orderSummaryMapper.selectList(query));
    }

    @PostMapping("/order")
    public Result<List<Trade>> placeOrder(@RequestBody OrderRequest request) {
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
    public Result<String> cancelOrder(@PathVariable String orderId, @RequestParam String userId) {
        return orderService.cancelOrder(orderId, userId);
    }

    @GetMapping("/order/book/{symbol}")
    public Result<OrderBookDTO> getOrderBook(@PathVariable String symbol) {
        OrderBook orderBook = matchingEngine.getOrderBook(symbol);
        System.out.println("Order Book: " + orderBook);
        return Result.success(new OrderBookDTO(orderBook.getBids(), orderBook.getAsks()));
    }
    
    @GetMapping("/balance/{userId}")
    public Result<Wallet> getBalance(@PathVariable String userId) {
        return Result.success(clearingService.getWallet(userId));
    }
    
    @PostMapping("/balance/deposit")
    public Result<String> deposit(@RequestBody DepositRequest request) {
            clearingService.deposit(request.getUserId(), request.getCurrency(), request.getAmount());
            return Result.success("Deposit successful");
    }

    @GetMapping("/orders/{userId}")
    public Result<List<com.uniswap.clone.entity.Order>> getOrders(@PathVariable String userId) {
        QueryWrapper<com.uniswap.clone.entity.Order> query = new QueryWrapper<>();
        query.eq("user_id", userId).orderByDesc("timestamp");
        return Result.success(orderMapper.selectList(query));
    }
    
    @GetMapping("/trades/{userId}")
    public Result<List<com.uniswap.clone.entity.Trade>> getTrades(@PathVariable String userId) {
        return Result.success(clearingService.getUserTrades(userId));
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
