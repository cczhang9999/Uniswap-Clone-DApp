package com.uniswap.clone.service;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.uniswap.clone.model.Trade;
import com.uniswap.clone.model.Wallet;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

@Service
public class ClearingService {

    private final com.uniswap.clone.mapper.DepositMapper depositMapper;
    private final com.uniswap.clone.mapper.BalanceMapper balanceMapper;
    private final StringRedisTemplate redisTemplate;
    private final ObjectMapper objectMapper = new ObjectMapper();

    public ClearingService(com.uniswap.clone.mapper.DepositMapper depositMapper, 
                           com.uniswap.clone.mapper.BalanceMapper balanceMapper,
                           StringRedisTemplate redisTemplate) {
        this.depositMapper = depositMapper;
        this.balanceMapper = balanceMapper;
        this.redisTemplate = redisTemplate;
    }

    public Wallet getWallet(String userId) {
        String cacheKey = "wallet:" + userId;
        
        // Try to get from Redis cache, but don't fail if Redis is unavailable
        try {
            String cachedWallet = redisTemplate.opsForValue().get(cacheKey);
            if (cachedWallet != null) {
                return objectMapper.readValue(cachedWallet, Wallet.class);
            }
        } catch (Exception e) {
            // Redis unavailable, continue without cache
            System.err.println("Redis cache unavailable, fetching from database: " + e.getMessage());
        }

        // Fetch from database
        QueryWrapper<com.uniswap.clone.entity.Balance> query = new QueryWrapper<>();
        query.eq("user_id", userId);
        List<com.uniswap.clone.entity.Balance> balanceEntities = balanceMapper.selectList(query);

        Wallet wallet = new Wallet(userId);
        Map<String, com.uniswap.clone.model.Balance> balanceMap = balanceEntities.stream()
                .collect(Collectors.toMap(
                        com.uniswap.clone.entity.Balance::getCurrency,
                        entity -> new com.uniswap.clone.model.Balance(
                                entity.getCurrency(),
                                entity.getAvailable(),
                                entity.getFrozen()
                        )
                ));
        wallet.setBalances(balanceMap);

        // Try to cache the result, but don't fail if Redis is unavailable
        try {
            redisTemplate.opsForValue().set(cacheKey, objectMapper.writeValueAsString(wallet), 10, TimeUnit.MINUTES);
        } catch (Exception e) {
            // Redis unavailable, skip caching
            System.err.println("Redis cache unavailable, skipping cache write: " + e.getMessage());
        }

        return wallet;
    }

    @Transactional
    public void deposit(String userId, String currency, BigDecimal amount) {
        com.uniswap.clone.entity.Balance balance = getOrCreateBalance(userId, currency);
        balance.setAvailable(balance.getAvailable().add(amount));
        balanceMapper.updateById(balance);

        // Persist deposit record
        com.uniswap.clone.entity.Deposit deposit = new com.uniswap.clone.entity.Deposit();
        deposit.setUserId(userId);
        deposit.setCurrency(currency);
        deposit.setAmount(amount);
        deposit.setTimestamp(System.currentTimeMillis());
        depositMapper.insert(deposit);
        
        invalidateCache(userId);
    }

    @Transactional
    public boolean freezeFunds(String userId, String currency, BigDecimal amount) {
        com.uniswap.clone.entity.Balance balance = getOrCreateBalance(userId, currency);
        
        if (balance.getAvailable().compareTo(amount) < 0) {
            return false; // Insufficient funds
        }

        balance.setAvailable(balance.getAvailable().subtract(amount));
        balance.setFrozen(balance.getFrozen().add(amount));
        balanceMapper.updateById(balance);
        
        invalidateCache(userId);
        return true;
    }

    @Transactional
    public void unfreezeFunds(String userId, String currency, BigDecimal amount) {
        com.uniswap.clone.entity.Balance balance = getOrCreateBalance(userId, currency);
        balance.setFrozen(balance.getFrozen().subtract(amount));
        balance.setAvailable(balance.getAvailable().add(amount));
        balanceMapper.updateById(balance);
        
        invalidateCache(userId);
    }

    @Transactional
    public void settleTrade(Trade trade) {
        String[] parts = trade.getSymbol().split("-");
        String baseCurrency = parts[0]; // BTC
        String quoteCurrency = parts[1]; // USDT

        BigDecimal quoteAmount = trade.getPrice().multiply(trade.getQuantity());
        BigDecimal baseAmount = trade.getQuantity();

        // 1. Buyer: Pays Quote (Frozen), Receives Base (Available)
        com.uniswap.clone.entity.Balance buyerQuote = getOrCreateBalance(trade.getBuyerUserId(), quoteCurrency);
        buyerQuote.setFrozen(buyerQuote.getFrozen().subtract(quoteAmount));
        balanceMapper.updateById(buyerQuote);

        com.uniswap.clone.entity.Balance buyerBase = getOrCreateBalance(trade.getBuyerUserId(), baseCurrency);
        buyerBase.setAvailable(buyerBase.getAvailable().add(baseAmount));
        balanceMapper.updateById(buyerBase);

        // 2. Seller: Pays Base (Frozen), Receives Quote (Available)
        com.uniswap.clone.entity.Balance sellerBase = getOrCreateBalance(trade.getSellerUserId(), baseCurrency);
        sellerBase.setFrozen(sellerBase.getFrozen().subtract(baseAmount));
        balanceMapper.updateById(sellerBase);

        com.uniswap.clone.entity.Balance sellerQuote = getOrCreateBalance(trade.getSellerUserId(), quoteCurrency);
        sellerQuote.setAvailable(sellerQuote.getAvailable().add(quoteAmount));
        balanceMapper.updateById(sellerQuote);
        
        invalidateCache(trade.getBuyerUserId());
        invalidateCache(trade.getSellerUserId());
    }

    private com.uniswap.clone.entity.Balance getOrCreateBalance(String userId, String currency) {
        QueryWrapper<com.uniswap.clone.entity.Balance> query = new QueryWrapper<>();
        query.eq("user_id", userId).eq("currency", currency);
        com.uniswap.clone.entity.Balance balance = balanceMapper.selectOne(query);

        if (balance == null) {
            balance = new com.uniswap.clone.entity.Balance();
            balance.setUserId(userId);
            balance.setCurrency(currency);
            balance.setAvailable(BigDecimal.ZERO);
            balance.setFrozen(BigDecimal.ZERO);
            balanceMapper.insert(balance);
        }
        return balance;
    }
    
    private void invalidateCache(String userId) {
        try {
            redisTemplate.delete("wallet:" + userId);
        } catch (Exception e) {
            // Redis unavailable, skip cache invalidation
            System.err.println("Redis cache unavailable, skipping cache invalidation: " + e.getMessage());
        }
    }
}
