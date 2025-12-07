package com.uniswap.clone.engine;

import com.uniswap.clone.model.Order;
import com.uniswap.clone.model.Trade;
import com.uniswap.clone.service.ClearingService;
import com.uniswap.clone.service.RiskEngine;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Service
public class MatchingEngine {
    private final Map<String, OrderBook> orderBooks = new ConcurrentHashMap<>();
    private final ClearingService clearingService;
    private final RiskEngine riskEngine;

    public MatchingEngine(ClearingService clearingService, RiskEngine riskEngine) {
        this.clearingService = clearingService;
        this.riskEngine = riskEngine;
    }

    public OrderBook getOrderBook(String symbol) {
        return orderBooks.computeIfAbsent(symbol, OrderBook::new);
    }

    public List<Trade> processOrder(Order order) {
        // 0. Pre-trade Risk Check
        riskEngine.preTradeCheck(order);

        // 1. Pre-trade Risk Check: Freeze Funds
        String[] parts = order.getSymbol().split("-");
        String baseCurrency = parts[0]; // BTC
        String quoteCurrency = parts[1]; // USDT
        
        String currencyToFreeze;
        BigDecimal amountToFreeze;

        if (order.getSide() == Order.Side.BUY) {
            // Buy BTC with USDT: Freeze USDT (Price * Quantity)
            currencyToFreeze = quoteCurrency;
            amountToFreeze = order.getPrice().multiply(order.getQuantity());
        } else {
            // Sell BTC for USDT: Freeze BTC (Quantity)
            currencyToFreeze = baseCurrency;
            amountToFreeze = order.getQuantity();
        }

        boolean frozen = clearingService.freezeFunds(order.getUserId(), currencyToFreeze, amountToFreeze);
        if (!frozen) {
            throw new RuntimeException("Insufficient funds for user: " + order.getUserId());
        }

        // 2. Match Order
        OrderBook orderBook = getOrderBook(order.getSymbol());
        List<Trade> trades;
        synchronized (orderBook) { 
            trades = orderBook.processOrder(order);
        }

        // 3. Post-trade Settlement
        for (Trade trade : trades) {
            clearingService.settleTrade(trade);
        }
        
        // Note: If order is partially filled and added to book, the remaining funds stay frozen.
        // This is correct behavior.
        
        return trades;
    }
}
