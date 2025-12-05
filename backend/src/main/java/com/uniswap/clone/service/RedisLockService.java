package com.uniswap.clone.service;

import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import java.util.UUID;
import java.util.concurrent.TimeUnit;

/**
 * Redis-based distributed lock service
 * Provides thread-safe lock acquisition and release across multiple application instances
 */
@Service
public class RedisLockService {

    private final StringRedisTemplate redisTemplate;
    
    // Default lock expiration time to prevent deadlocks
    private static final long DEFAULT_LOCK_EXPIRE_TIME = 30;
    private static final TimeUnit DEFAULT_TIME_UNIT = TimeUnit.SECONDS;

    public RedisLockService(StringRedisTemplate redisTemplate) {
        this.redisTemplate = redisTemplate;
    }

    /**
     * Try to acquire a distributed lock
     * 
     * @param lockKey The key for the lock
     * @param timeout Maximum time to wait for lock acquisition
     * @param unit Time unit for timeout
     * @return LockResult containing success status and lock value (for unlock)
     */
    public LockResult tryLock(String lockKey, long timeout, TimeUnit unit) {
        String lockValue = UUID.randomUUID().toString();
        long startTime = System.currentTimeMillis();
        long timeoutMillis = unit.toMillis(timeout);
        
        try {
            // Try to acquire lock with retry logic
            while (System.currentTimeMillis() - startTime < timeoutMillis) {
                // SET NX EX: Set if Not eXists with EXpiration
                Boolean acquired = redisTemplate.opsForValue()
                    .setIfAbsent(lockKey, lockValue, DEFAULT_LOCK_EXPIRE_TIME, DEFAULT_TIME_UNIT);
                
                if (Boolean.TRUE.equals(acquired)) {
                    return new LockResult(true, lockValue);
                }
                
                // Wait a bit before retrying
                Thread.sleep(50);
            }
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            return new LockResult(false, null);
        } catch (Exception e) {
            // If Redis is unavailable, log and return failure
            System.err.println("Failed to acquire lock: " + e.getMessage());
            return new LockResult(false, null);
        }
        
        return new LockResult(false, null);
    }

    /**
     * Release the distributed lock
     * Only releases if the lock value matches (prevents accidental unlock by other threads)
     * 
     * @param lockKey The key for the lock
     * @param lockValue The value that was returned when lock was acquired
     */
    public void unlock(String lockKey, String lockValue) {
        if (lockValue == null) {
            return;
        }
        
        try {
            String currentValue = redisTemplate.opsForValue().get(lockKey);
            // Only delete if the value matches (prevents unlocking someone else's lock)
            if (lockValue.equals(currentValue)) {
                redisTemplate.delete(lockKey);
            }
        } catch (Exception e) {
            System.err.println("Failed to release lock: " + e.getMessage());
        }
    }

    /**
     * Result of lock acquisition attempt
     */
    public static class LockResult {
        private final boolean acquired;
        private final String lockValue;

        public LockResult(boolean acquired, String lockValue) {
            this.acquired = acquired;
            this.lockValue = lockValue;
        }

        public boolean isAcquired() {
            return acquired;
        }

        public String getLockValue() {
            return lockValue;
        }
    }
}
