package com.uniswap.clone.service;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.UpdateWrapper;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.uniswap.clone.entity.Balance;
import com.uniswap.clone.entity.Deposit;
import com.uniswap.clone.mapper.BalanceMapper;
import com.uniswap.clone.mapper.DepositMapper;
import com.uniswap.clone.mapper.TradeMapper;
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

    private final DepositMapper depositMapper;
    private final BalanceMapper balanceMapper;
    private final TradeMapper tradeMapper;
    private final StringRedisTemplate redisTemplate;
    private final RedisLockService redisLockService;
    private final ObjectMapper objectMapper = new ObjectMapper();

    public ClearingService(DepositMapper depositMapper, 
                           BalanceMapper balanceMapper,
                           TradeMapper tradeMapper,
                           StringRedisTemplate redisTemplate,
                           RedisLockService redisLockService) {
        this.depositMapper = depositMapper;
        this.balanceMapper = balanceMapper;
        this.tradeMapper = tradeMapper;
        this.redisTemplate = redisTemplate;
        this.redisLockService = redisLockService;
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
        QueryWrapper<Balance> query = new QueryWrapper<>();
        query.eq("user_id", userId);
        List<Balance> balanceEntities = balanceMapper.selectList(query);

        Wallet wallet = new Wallet(userId);
        Map<String, com.uniswap.clone.model.Balance> balanceMap = balanceEntities.stream()
                .collect(Collectors.toMap(
                        Balance::getCurrency,
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

    @Transactional(rollbackFor = Exception.class)
    public void deposit(String userId, String currency, BigDecimal amount) {
        // Create lock key based on userId and currency
        String lockKey = "deposit:lock:" + userId + ":" + currency;
        
        // Try to acquire distributed lock (wait up to 5 seconds)
        RedisLockService.LockResult lockResult = redisLockService.tryLock(lockKey, 5, TimeUnit.SECONDS);
        
        // If Redis is unavailable, log warning and continue without lock (for development)
        // In production, you should ensure Redis is always available
        if (!lockResult.isAcquired()) {
            System.err.println("WARNING: Failed to acquire distributed lock for deposit. " +
                             "Proceeding without lock. userId=" + userId + ", currency=" + currency);
            // Continue without lock - not ideal but allows development to proceed
        }
        
        try {
            // Execute deposit logic within lock (or without if Redis unavailable)
            Balance balance = getOrCreateBalance(userId, currency);
            BigDecimal newAvailable = balance.getAvailable().add(amount);
            
            UpdateWrapper<Balance> updateWrapper = new UpdateWrapper<>();
            updateWrapper.eq("user_id", userId)
                        .eq("currency", currency)
                        .set("available", newAvailable);
            balanceMapper.update(null, updateWrapper);

            // Persist deposit record
            Deposit deposit = new Deposit();
            deposit.setUserId(userId);
            deposit.setCurrency(currency);
            deposit.setAmount(amount);
            deposit.setTimestamp(System.currentTimeMillis());
            depositMapper.insert(deposit);
            
            invalidateCache(userId);
        } finally {
            // Always try to release the lock if it was acquired
            if (lockResult.isAcquired()) {
                redisLockService.unlock(lockKey, lockResult.getLockValue());
            }
        }
    }

    @Transactional
    public boolean freezeFunds(String userId, String currency, BigDecimal amount) {
        String lockKey = "balance:lock:" + userId + ":" + currency;
        // Try to acquire distributed lock (wait up to 5 seconds)
        // Note: For critical financial operations, we must ensure lock acquisition or fail
        RedisLockService.LockResult lockResult = redisLockService.tryLock(lockKey, 5, TimeUnit.SECONDS);
        
        if (!lockResult.isAcquired()) {
            System.err.println("Failed to acquire lock for freezeFunds. userId=" + userId);
            throw new RuntimeException("System busy, please try again");
        }
        
        try {
            Balance balance = getOrCreateBalance(userId, currency);
            
            if (balance.getAvailable().compareTo(amount) < 0) {
                return false; // Insufficient funds
            }

            BigDecimal newAvailable = balance.getAvailable().subtract(amount);
            BigDecimal newFrozen = balance.getFrozen().add(amount);
            
            UpdateWrapper<Balance> updateWrapper = new UpdateWrapper<>();
            updateWrapper.eq("user_id", userId)
                        .eq("currency", currency)
                        .set("available", newAvailable)
                        .set("frozen", newFrozen);
            balanceMapper.update(null, updateWrapper);
            
            invalidateCache(userId);
            return true;
        } finally {
            redisLockService.unlock(lockKey, lockResult.getLockValue());
        }
    }

    @Transactional
    public void unfreezeFunds(String userId, String currency, BigDecimal amount) {
        String lockKey = "balance:lock:" + userId + ":" + currency;
        RedisLockService.LockResult lockResult = redisLockService.tryLock(lockKey, 5, TimeUnit.SECONDS);
        
        if (!lockResult.isAcquired()) {
            throw new RuntimeException("System busy, please try again");
        }
        
        try {
            Balance balance = getOrCreateBalance(userId, currency);
            BigDecimal newFrozen = balance.getFrozen().subtract(amount);
            BigDecimal newAvailable = balance.getAvailable().add(amount);
            
            UpdateWrapper<Balance> updateWrapper = new UpdateWrapper<>();
            updateWrapper.eq("user_id", userId)
                        .eq("currency", currency)
                        .set("available", newAvailable)
                        .set("frozen", newFrozen);
            balanceMapper.update(null, updateWrapper);
            
            invalidateCache(userId);
        } finally {
            redisLockService.unlock(lockKey, lockResult.getLockValue());
        }
    }

    @Transactional
    public void settleTrade(Trade trade) {
        System.out.println("Processing trade settlement: " + trade);

        String[] parts = trade.getSymbol().split("-");
        String baseCurrency = parts[0]; // BTC
        String quoteCurrency = parts[1]; // USDT

        BigDecimal quoteAmount = trade.getPrice().multiply(trade.getQuantity());
        BigDecimal baseAmount = trade.getQuantity();

        // 1. Buyer: Pays Quote (Frozen), Receives Base (Available)
        Balance buyerQuote = getOrCreateBalance(trade.getBuyerUserId(), quoteCurrency);
        BigDecimal buyerQuoteNewFrozen = buyerQuote.getFrozen().subtract(quoteAmount);
        
        UpdateWrapper<Balance> buyerQuoteWrapper = new UpdateWrapper<>();
        buyerQuoteWrapper.eq("user_id", trade.getBuyerUserId())
                        .eq("currency", quoteCurrency)
                        .set("frozen", buyerQuoteNewFrozen);
        balanceMapper.update(null, buyerQuoteWrapper);

        Balance buyerBase = getOrCreateBalance(trade.getBuyerUserId(), baseCurrency);
        BigDecimal buyerBaseNewAvailable = buyerBase.getAvailable().add(baseAmount);
        
        UpdateWrapper<Balance> buyerBaseWrapper = new UpdateWrapper<>();
        buyerBaseWrapper.eq("user_id", trade.getBuyerUserId())
                       .eq("currency", baseCurrency)
                       .set("available", buyerBaseNewAvailable);
        balanceMapper.update(null, buyerBaseWrapper);

        // 2. Seller: Pays Base (Frozen), Receives Quote (Available)
        Balance sellerBase = getOrCreateBalance(trade.getSellerUserId(), baseCurrency);
        BigDecimal sellerBaseNewFrozen = sellerBase.getFrozen().subtract(baseAmount);
        
        UpdateWrapper<Balance> sellerBaseWrapper = new UpdateWrapper<>();
        sellerBaseWrapper.eq("user_id", trade.getSellerUserId())
                        .eq("currency", baseCurrency)
                        .set("frozen", sellerBaseNewFrozen);
        balanceMapper.update(null, sellerBaseWrapper);

        Balance sellerQuote = getOrCreateBalance(trade.getSellerUserId(), quoteCurrency);
        BigDecimal sellerQuoteNewAvailable = sellerQuote.getAvailable().add(quoteAmount);
        
        UpdateWrapper<Balance> sellerQuoteWrapper = new UpdateWrapper<>();
        sellerQuoteWrapper.eq("user_id", trade.getSellerUserId())
                         .eq("currency", quoteCurrency)
                         .set("available", sellerQuoteNewAvailable);
        balanceMapper.update(null, sellerQuoteWrapper);
        
        // 3. Persist Trade Record
        com.uniswap.clone.entity.Trade tradeEntity = new com.uniswap.clone.entity.Trade();
        tradeEntity.setTradeId(trade.getTradeId());
        tradeEntity.setBuyerUserId(trade.getBuyerUserId());
        tradeEntity.setSellerUserId(trade.getSellerUserId());
        tradeEntity.setSymbol(trade.getSymbol());
        tradeEntity.setBuyOrderId(trade.getBuyOrderId());
        tradeEntity.setSellOrderId(trade.getSellOrderId());
        tradeEntity.setPrice(trade.getPrice());
        tradeEntity.setQuantity(trade.getQuantity());
        tradeEntity.setTimestamp(trade.getTimestamp());
        System.out.println("3333333333333");
        tradeMapper.insert(tradeEntity);
        System.out.println("Trade persisted to database: " + tradeEntity.getTradeId());
        
        invalidateCache(trade.getBuyerUserId());
        invalidateCache(trade.getSellerUserId());
    }

    private Balance getOrCreateBalance(String userId, String currency) {
        QueryWrapper<Balance> query = new QueryWrapper<>();
        query.eq("user_id", userId).eq("currency", currency);
        Balance balance = balanceMapper.selectOne(query);

        if (balance == null) {
            balance = new Balance();
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

    public List<com.uniswap.clone.entity.Trade> getUserTrades(String userId) {
        QueryWrapper<com.uniswap.clone.entity.Trade> query = new QueryWrapper<>();
        // Fetch trades where user is either buyer OR seller
        query.eq("buyer_user_id", userId).or().eq("seller_user_id", userId)
             .orderByDesc("timestamp");
        return tradeMapper.selectList(query);
    }
}
