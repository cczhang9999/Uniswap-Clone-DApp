package com.uniswap.clone.engine;

import com.uniswap.clone.mapper.BalanceMapper;
import com.uniswap.clone.mapper.DepositMapper;
import com.uniswap.clone.model.Order;
import com.uniswap.clone.model.Trade;
import com.uniswap.clone.service.ClearingService;
import com.uniswap.clone.service.RedisLockService;
import com.uniswap.clone.service.RiskEngine;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.data.redis.core.StringRedisTemplate;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

class MatchingEngineTest {

    private MatchingEngine matchingEngine;
    private ClearingService clearingService;
    private RiskEngine riskEngine;
    
    @Mock
    private DepositMapper depositMapper;
    
    @Mock
    private BalanceMapper balanceMapper;
    
    @Mock
    private StringRedisTemplate redisTemplate;
    
    @Mock
    private com.uniswap.clone.mapper.TradeMapper tradeMapper;
    
    @Mock
    private RedisLockService redisLockService;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        clearingService = new ClearingService(depositMapper, balanceMapper, tradeMapper, redisTemplate, redisLockService);
        riskEngine = new RiskEngine();
        matchingEngine = new MatchingEngine(clearingService, riskEngine);
    }

    @Test
    void testSimpleMatch() {
        String symbol = "BTC-USDT";
        String sellerId = "user1";
        String buyerId = "user2";
        
        // Setup funds
        clearingService.deposit(sellerId, "BTC", new BigDecimal("10"));
        clearingService.deposit(buyerId, "USDT", new BigDecimal("100000"));

        // 1. Place Sell Order: 1 BTC @ 50000
        Order sellOrder = new Order(sellerId, UUID.randomUUID().toString(), symbol, Order.Side.SELL, Order.Type.LIMIT, new BigDecimal("50000"), new BigDecimal("1"), System.currentTimeMillis());
        matchingEngine.processOrder(sellOrder);

        // 2. Place Buy Order: 1 BTC @ 50000
        Order buyOrder = new Order(buyerId, UUID.randomUUID().toString(), symbol, Order.Side.BUY, Order.Type.LIMIT, new BigDecimal("50000"), new BigDecimal("1"), System.currentTimeMillis());
        List<Trade> trades = matchingEngine.processOrder(buyOrder);

        assertEquals(1, trades.size());
        assertEquals(new BigDecimal("50000"), trades.get(0).getPrice());
        assertEquals(new BigDecimal("1"), trades.get(0).getQuantity());
        
        // Verify Balances
        // Seller: -1 BTC, +50000 USDT
        assertEquals(new BigDecimal("9"), clearingService.getWallet(sellerId).getBalances().get("BTC").getAvailable());
        assertEquals(new BigDecimal("50000"), clearingService.getWallet(sellerId).getBalances().get("USDT").getAvailable());
        
        // Buyer: +1 BTC, -50000 USDT
        assertEquals(new BigDecimal("1"), clearingService.getWallet(buyerId).getBalances().get("BTC").getAvailable());
        assertEquals(new BigDecimal("50000"), clearingService.getWallet(buyerId).getBalances().get("USDT").getAvailable());
    }

    @Test
    void testPricePriority() {
        String symbol = "ETH-USDT";
        String seller1 = "s1";
        String seller2 = "s2";
        String buyer = "b1";
        
        clearingService.deposit(seller1, "ETH", new BigDecimal("10"));
        clearingService.deposit(seller2, "ETH", new BigDecimal("10"));
        clearingService.deposit(buyer, "USDT", new BigDecimal("10000"));

        // Sell 1 ETH @ 3000
        matchingEngine.processOrder(new Order(seller1, UUID.randomUUID().toString(), symbol, Order.Side.SELL, Order.Type.LIMIT, new BigDecimal("3000"), new BigDecimal("1"), System.currentTimeMillis()));
        // Sell 1 ETH @ 2900 (Better price for buyer)
        matchingEngine.processOrder(new Order(seller2, UUID.randomUUID().toString(), symbol, Order.Side.SELL, Order.Type.LIMIT, new BigDecimal("2900"), new BigDecimal("1"), System.currentTimeMillis()));

        // Buy 1 ETH @ 3100
        Order buyOrder = new Order(buyer, UUID.randomUUID().toString(), symbol, Order.Side.BUY, Order.Type.LIMIT, new BigDecimal("3100"), new BigDecimal("1"), System.currentTimeMillis());
        List<Trade> trades = matchingEngine.processOrder(buyOrder);

        assertEquals(1, trades.size());
        assertEquals(new BigDecimal("2900"), trades.get(0).getPrice()); // Should match with the cheaper seller first
    }
    
    @Test
    void testPartialFill() {
        String symbol = "SOL-USDT";
        String seller = "s1";
        String buyer = "b1";
        
        clearingService.deposit(seller, "SOL", new BigDecimal("10"));
        clearingService.deposit(buyer, "USDT", new BigDecimal("1000"));
        
        // Sell 2 SOL @ 100
        matchingEngine.processOrder(new Order(seller, UUID.randomUUID().toString(), symbol, Order.Side.SELL, Order.Type.LIMIT, new BigDecimal("100"), new BigDecimal("2"), System.currentTimeMillis()));
        
        // Buy 1 SOL @ 100
        Order buyOrder = new Order(buyer, UUID.randomUUID().toString(), symbol, Order.Side.BUY, Order.Type.LIMIT, new BigDecimal("100"), new BigDecimal("1"), System.currentTimeMillis());
        List<Trade> trades = matchingEngine.processOrder(buyOrder);
        
        assertEquals(1, trades.size());
        assertEquals(new BigDecimal("1"), trades.get(0).getQuantity());
        
        // Check remaining order book
        OrderBook book = matchingEngine.getOrderBook(symbol);
        assertEquals(1, book.getAsks().size());
        assertEquals(new BigDecimal("1"), book.getAsks().get(0).getQuantity());
    }
    
    @Test
    void testInsufficientFunds() {
        String symbol = "BTC-USDT";
        String buyer = "b1";
        // No deposit
        
        Order buyOrder = new Order(buyer, UUID.randomUUID().toString(), symbol, Order.Side.BUY, Order.Type.LIMIT, new BigDecimal("50000"), new BigDecimal("1"), System.currentTimeMillis());
        
        assertThrows(RuntimeException.class, () -> {
            matchingEngine.processOrder(buyOrder);
        });
    }

    @Test
    void testRiskCheck() {
        String symbol = "BTC-USDT";
        String buyer = "b1";
        
        // Order too large (> 1000)
        Order hugeOrder = new Order(buyer, UUID.randomUUID().toString(), symbol, Order.Side.BUY, Order.Type.LIMIT, new BigDecimal("50000"), new BigDecimal("2000"), System.currentTimeMillis());
        
        RuntimeException exception = assertThrows(RuntimeException.class, () -> {
            matchingEngine.processOrder(hugeOrder);
        });
        assertTrue(exception.getMessage().contains("Order quantity too large"));
    }
}
