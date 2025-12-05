package com.uniswap.clone.controller;

import com.uniswap.clone.engine.MatchingEngine;
import com.uniswap.clone.engine.OrderBook;
import com.uniswap.clone.model.Order;
import com.uniswap.clone.model.Trade;
import com.uniswap.clone.model.Wallet;
import com.uniswap.clone.service.ClearingService;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1")
@CrossOrigin(origins = "http://localhost:3000")
public class OrderController {

    private final MatchingEngine matchingEngine;
    private final ClearingService clearingService;

    public OrderController(MatchingEngine matchingEngine, ClearingService clearingService) {
        this.matchingEngine = matchingEngine;
        this.clearingService = clearingService;
    }

    @PostMapping("/order")
    public com.uniswap.clone.common.Result<List<Trade>> placeOrder(@RequestBody OrderRequest request) {
        Order order = new Order(
                request.getUserId(),
                UUID.randomUUID().toString(),
                request.getSymbol(),
                request.getSide(),
                request.getType(),
                request.getPrice(),
                request.getQuantity(),
                System.currentTimeMillis()
        );
        return com.uniswap.clone.common.Result.success(matchingEngine.processOrder(order));
    }

    @GetMapping("/order/book/{symbol}")
    public com.uniswap.clone.common.Result<OrderBookDTO> getOrderBook(@PathVariable String symbol) {
        OrderBook orderBook = matchingEngine.getOrderBook(symbol);
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
