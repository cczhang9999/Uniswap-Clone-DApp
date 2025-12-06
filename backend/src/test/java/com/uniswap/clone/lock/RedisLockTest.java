package com.uniswap.clone.lock;

import com.uniswap.clone.service.RedisLockService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.redis.core.StringRedisTemplate;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicInteger;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Redis 分布式锁测试
 * 测试场景：
 * 1. 基本加锁/解锁
 * 2. 并发竞争锁
 * 3. 锁超时机制
 * 4. 防止误解锁（锁的所有权验证）
 * 5. 高并发场景下的计数器安全性
 */
@SpringBootTest
public class RedisLockTest {

    @Autowired
    private RedisLockService redisLockService;

    @Autowired
    private StringRedisTemplate redisTemplate;

    private static final String TEST_LOCK_KEY = "test:lock:counter";

    /**
     * 测试1: 基本的加锁和解锁功能
     */
    @Test
    public void testBasicLockAndUnlock() {
        String lockKey = "test:lock:basic";
        
        // 获取锁
        RedisLockService.LockResult result = redisLockService.tryLock(lockKey, 5, TimeUnit.SECONDS);
        assertTrue(result.isAcquired(), "应该成功获取锁");
        assertNotNull(result.getLockValue(), "锁值不应为空");
        
        // 验证 Redis 中存在该锁
        String value = redisTemplate.opsForValue().get(lockKey);
        assertEquals(result.getLockValue(), value, "Redis 中的锁值应该匹配");
        
        // 释放锁
        redisLockService.unlock(lockKey, result.getLockValue());
        
        // 验证锁已被释放
        String valueAfterUnlock = redisTemplate.opsForValue().get(lockKey);
        assertNull(valueAfterUnlock, "锁释放后 Redis 中不应存在该键");
        
        System.out.println("✅ 基本加锁/解锁测试通过");
    }

    /**
     * 测试2: 锁竞争 - 同一时刻只有一个线程能获取锁
     */
    @Test
    public void testLockCompetition() throws InterruptedException {
        String lockKey = "test:lock:competition";
        
        // 线程1先获取锁
        RedisLockService.LockResult result1 = redisLockService.tryLock(lockKey, 5, TimeUnit.SECONDS);
        assertTrue(result1.isAcquired(), "线程1应该成功获取锁");
        
        // 线程2尝试获取同一把锁（应该失败，因为等待时间很短）
        RedisLockService.LockResult result2 = redisLockService.tryLock(lockKey, 100, TimeUnit.MILLISECONDS);
        assertFalse(result2.isAcquired(), "线程2应该获取锁失败（锁被线程1持有）");
        
        // 线程1释放锁
        redisLockService.unlock(lockKey, result1.getLockValue());
        
        // 线程2再次尝试获取锁（应该成功）
        RedisLockService.LockResult result3 = redisLockService.tryLock(lockKey, 5, TimeUnit.SECONDS);
        assertTrue(result3.isAcquired(), "线程2应该成功获取锁（线程1已释放）");
        
        // 清理
        redisLockService.unlock(lockKey, result3.getLockValue());
        
        System.out.println("✅ 锁竞争测试通过");
    }

    /**
     * 测试3: 防止误解锁 - 只有锁的持有者才能解锁
     */
    @Test
    public void testPreventWrongUnlock() {
        String lockKey = "test:lock:wrong-unlock";
        
        // 线程1获取锁
        RedisLockService.LockResult result1 = redisLockService.tryLock(lockKey, 5, TimeUnit.SECONDS);
        assertTrue(result1.isAcquired());
        
        // 线程2尝试用错误的 lockValue 解锁（应该失败）
        String wrongLockValue = "wrong-lock-value-12345";
        redisLockService.unlock(lockKey, wrongLockValue);
        
        // 验证锁仍然存在（未被错误解锁）
        String value = redisTemplate.opsForValue().get(lockKey);
        assertNotNull(value, "锁不应被错误的 lockValue 解锁");
        assertEquals(result1.getLockValue(), value, "锁值应该保持不变");
        
        // 正确解锁
        redisLockService.unlock(lockKey, result1.getLockValue());
        assertNull(redisTemplate.opsForValue().get(lockKey), "正确的 lockValue 应该能解锁");
        
        System.out.println("✅ 防止误解锁测试通过");
    }

    /**
     * 测试4: 高并发场景 - 100个线程并发对计数器+1
     * 使用分布式锁保证计数器的准确性
     */
    @Test
    public void testConcurrentCounterWithLock() throws InterruptedException {
        String lockKey = "test:lock:counter";
        String counterKey = "test:counter";
        int threadCount = 100;
        int incrementsPerThread = 10;
        
        // 初始化计数器
        redisTemplate.opsForValue().set(counterKey, "0");
        
        // 创建线程池
        ExecutorService executor = Executors.newFixedThreadPool(20);
        CountDownLatch latch = new CountDownLatch(threadCount);
        AtomicInteger successCount = new AtomicInteger(0);
        
        // 提交任务
        for (int i = 0; i < threadCount; i++) {
            executor.submit(() -> {
                try {
                    for (int j = 0; j < incrementsPerThread; j++) {
                        // 获取分布式锁
                        RedisLockService.LockResult lockResult = redisLockService.tryLock(
                            lockKey, 10, TimeUnit.SECONDS
                        );
                        
                        if (lockResult.isAcquired()) {
                            try {
                                // 临界区：读取-修改-写入
                                String currentValue = redisTemplate.opsForValue().get(counterKey);
                                int current = Integer.parseInt(currentValue);
                                int newValue = current + 1;
                                redisTemplate.opsForValue().set(counterKey, String.valueOf(newValue));
                                
                                successCount.incrementAndGet();
                            } finally {
                                // 释放锁
                                redisLockService.unlock(lockKey, lockResult.getLockValue());
                            }
                        }
                    }
                } finally {
                    latch.countDown();
                }
            });
        }
        
        // 等待所有线程完成
        latch.await(30, TimeUnit.SECONDS);
        executor.shutdown();
        
        // 验证结果
        String finalValue = redisTemplate.opsForValue().get(counterKey);
        int expectedValue = threadCount * incrementsPerThread;
        
        System.out.println("期望值: " + expectedValue);
        System.out.println("实际值: " + finalValue);
        System.out.println("成功操作次数: " + successCount.get());
        
        assertEquals(String.valueOf(expectedValue), finalValue, 
            "使用分布式锁后，计数器应该准确无误");
        
        // 清理
        redisTemplate.delete(counterKey);
        
        System.out.println("✅ 高并发计数器测试通过");
    }

    /**
     * 测试5: 对比测试 - 不使用锁的并发计数器（会出现数据不一致）
     * 这个测试用于演示不加锁的危险性
     */
    @Test
    public void testConcurrentCounterWithoutLock() throws InterruptedException {
        String counterKey = "test:counter:nolock";
        int threadCount = 50;
        int incrementsPerThread = 10;
        
        // 初始化计数器
        redisTemplate.opsForValue().set(counterKey, "0");
        
        ExecutorService executor = Executors.newFixedThreadPool(20);
        CountDownLatch latch = new CountDownLatch(threadCount);
        
        for (int i = 0; i < threadCount; i++) {
            executor.submit(() -> {
                try {
                    for (int j = 0; j < incrementsPerThread; j++) {
                        // 不加锁的读取-修改-写入（危险！）
                        String currentValue = redisTemplate.opsForValue().get(counterKey);
                        int current = Integer.parseInt(currentValue);
                        int newValue = current + 1;
                        redisTemplate.opsForValue().set(counterKey, String.valueOf(newValue));
                    }
                } finally {
                    latch.countDown();
                }
            });
        }
        
        latch.await(30, TimeUnit.SECONDS);
        executor.shutdown();
        
        String finalValue = redisTemplate.opsForValue().get(counterKey);
        int expectedValue = threadCount * incrementsPerThread;
        
        System.out.println("期望值: " + expectedValue);
        System.out.println("实际值（无锁）: " + finalValue);
        
        // 不加锁的情况下，最终值很可能小于期望值（数据丢失）
        int actual = Integer.parseInt(finalValue);
        if (actual < expectedValue) {
            System.out.println("⚠️  数据丢失: " + (expectedValue - actual) + " 次更新");
            System.out.println("✅ 成功演示了不加锁的危险性");
        } else {
            System.out.println("⚠️  运气好，没有出现数据丢失（但仍然不安全）");
        }
        
        // 清理
        redisTemplate.delete(counterKey);
    }

    /**
     * 测试6: 锁超时自动释放
     * 模拟持有锁的线程崩溃，验证锁会自动过期
     */
    @Test
    public void testLockAutoExpire() throws InterruptedException {
        String lockKey = "test:lock:expire";
        
        // 获取锁（30秒过期）
        RedisLockService.LockResult result = redisLockService.tryLock(lockKey, 5, TimeUnit.SECONDS);
        assertTrue(result.isAcquired());
        
        // 不手动释放锁，模拟线程崩溃
        // 等待锁自动过期（RedisLockService 默认 30 秒过期）
        System.out.println("等待锁自动过期...");
        
        // 验证锁存在
        assertNotNull(redisTemplate.opsForValue().get(lockKey), "锁应该存在");
        
        // 等待超过过期时间
        Thread.sleep(31000); // 等待 31 秒
        
        // 验证锁已自动过期
        String value = redisTemplate.opsForValue().get(lockKey);
        assertNull(value, "锁应该已自动过期");
        
        System.out.println("✅ 锁超时自动释放测试通过");
    }

    /**
     * 测试7: 多个不同的锁互不影响
     */
    @Test
    public void testMultipleLocks() {
        String lockKey1 = "test:lock:key1";
        String lockKey2 = "test:lock:key2";
        
        // 同时获取两个不同的锁
        RedisLockService.LockResult result1 = redisLockService.tryLock(lockKey1, 5, TimeUnit.SECONDS);
        RedisLockService.LockResult result2 = redisLockService.tryLock(lockKey2, 5, TimeUnit.SECONDS);
        
        assertTrue(result1.isAcquired(), "锁1应该成功获取");
        assertTrue(result2.isAcquired(), "锁2应该成功获取");
        assertNotEquals(result1.getLockValue(), result2.getLockValue(), "两个锁的值应该不同");
        
        // 释放锁1不影响锁2
        redisLockService.unlock(lockKey1, result1.getLockValue());
        assertNull(redisTemplate.opsForValue().get(lockKey1), "锁1应该被释放");
        assertNotNull(redisTemplate.opsForValue().get(lockKey2), "锁2应该仍然存在");
        
        // 清理
        redisLockService.unlock(lockKey2, result2.getLockValue());
        
        System.out.println("✅ 多锁独立性测试通过");
    }

    /**
     * 测试8: 模拟真实业务场景 - 账户余额扣减
     * 多个线程同时对同一账户进行扣款操作
     */
    @Test
    public void testAccountBalanceDeduction() throws InterruptedException {
        String accountId = "user:1001";
        String balanceKey = "balance:" + accountId;
        String lockKey = "lock:balance:" + accountId;
        
        // 初始余额 1000
        redisTemplate.opsForValue().set(balanceKey, "1000");
        
        int threadCount = 20;
        int deductionAmount = 10; // 每次扣 10
        
        ExecutorService executor = Executors.newFixedThreadPool(10);
        CountDownLatch latch = new CountDownLatch(threadCount);
        List<Boolean> results = new CopyOnWriteArrayList<>();
        
        for (int i = 0; i < threadCount; i++) {
            executor.submit(() -> {
                try {
                    RedisLockService.LockResult lockResult = redisLockService.tryLock(
                        lockKey, 10, TimeUnit.SECONDS
                    );
                    
                    if (lockResult.isAcquired()) {
                        try {
                            // 读取余额
                            String balanceStr = redisTemplate.opsForValue().get(balanceKey);
                            int balance = Integer.parseInt(balanceStr);
                            
                            // 检查余额是否足够
                            if (balance >= deductionAmount) {
                                // 模拟业务处理延迟
                                Thread.sleep(10);
                                
                                // 扣减余额
                                int newBalance = balance - deductionAmount;
                                redisTemplate.opsForValue().set(balanceKey, String.valueOf(newBalance));
                                results.add(true); // 扣款成功
                            } else {
                                results.add(false); // 余额不足
                            }
                        } catch (InterruptedException e) {
                            Thread.currentThread().interrupt();
                        } finally {
                            redisLockService.unlock(lockKey, lockResult.getLockValue());
                        }
                    }
                } finally {
                    latch.countDown();
                }
            });
        }
        
        latch.await(30, TimeUnit.SECONDS);
        executor.shutdown();
        
        // 验证结果
        String finalBalance = redisTemplate.opsForValue().get(balanceKey);
        long successCount = results.stream().filter(r -> r).count();
        
        System.out.println("初始余额: 1000");
        System.out.println("扣款次数: " + threadCount);
        System.out.println("每次扣款: " + deductionAmount);
        System.out.println("成功扣款: " + successCount + " 次");
        System.out.println("最终余额: " + finalBalance);
        
        int expectedBalance = 1000 - (int)successCount * deductionAmount;
        assertEquals(String.valueOf(expectedBalance), finalBalance, "余额应该准确");
        
        // 清理
        redisTemplate.delete(balanceKey);
        
        System.out.println("✅ 账户余额扣减测试通过");
    }
}
