package com.uniswap.clone.config;

import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.core.StringRedisTemplate;

/**
 * Redis connection test configuration
 * Uncomment the @Bean annotation to enable Redis connection testing on startup
 */
@Configuration
public class RedisTestConfig {
    
    
    // Enable Redis connection test to verify RESP2 fix
    @Bean
    public CommandLineRunner testRedisConnection(StringRedisTemplate redisTemplate) {
        return args -> {
            try {
                System.out.println("========== Testing Redis Connection ==========");
                
                // Test 1: Ping
                String pong = redisTemplate.getConnectionFactory()
                    .getConnection()
                    .ping();
                System.out.println("✓ Redis PING: " + pong);
                
                // Test 2: Set and Get
                redisTemplate.opsForValue().set("test:key", "Hello Redis!");
                String value = redisTemplate.opsForValue().get("test:key");
                System.out.println("✓ Redis SET/GET: " + value);
                
                // Test 3: Delete
                redisTemplate.delete("test:key");
                System.out.println("✓ Redis DELETE: success");
                
                System.out.println("========== Redis Connection Test PASSED ==========");
            } catch (Exception e) {
                System.err.println("========== Redis Connection Test FAILED ==========");
                System.err.println("Error: " + e.getMessage());
                e.printStackTrace();
            }
        };
    }
}
