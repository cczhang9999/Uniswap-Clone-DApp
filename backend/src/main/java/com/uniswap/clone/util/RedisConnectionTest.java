package com.uniswap.clone.util;

import io.lettuce.core.RedisClient;
import io.lettuce.core.RedisURI;
import io.lettuce.core.api.StatefulRedisConnection;
import io.lettuce.core.api.sync.RedisCommands;

/**
 * Redis connection diagnostic tool
 * Run this to test different connection scenarios
 */
public class RedisConnectionTest {
    
    public static void main(String[] args) {
        String host = "194.164.194.118";
        int port = 9502;
        String password = "pass123editmelol";
        
        System.out.println("========== Redis Connection Diagnostic ==========");
        System.out.println("Host: " + host);
        System.out.println("Port: " + port);
        System.out.println("Password: " + (password != null ? "***" : "none"));
        System.out.println("=================================================\n");
        
        // Test 1: Connection without SSL, without password
        testConnection(host, port, null, false, "Test 1: No SSL, No Auth");
        
        // Test 2: Connection without SSL, with password
        testConnection(host, port, password, false, "Test 2: No SSL, With Auth");
        
        // Test 3: Connection with SSL, without password
        testConnection(host, port, null, true, "Test 3: SSL, No Auth");
        
        // Test 4: Connection with SSL, with password
        testConnection(host, port, password, true, "Test 4: SSL, With Auth");
    }
    
    private static void testConnection(String host, int port, String password, boolean useSsl, String testName) {
        System.out.println("\n" + testName);
        System.out.println("----------------------------------------");
        
        RedisClient redisClient = null;
        StatefulRedisConnection<String, String> connection = null;
        
        try {
            // Build Redis URI
            RedisURI.Builder builder = RedisURI.builder()
                .withHost(host)
                .withPort(port);
            
            if (password != null && !password.isEmpty()) {
                builder.withPassword(password.toCharArray());
            }
            
            if (useSsl) {
                builder.withSsl(true);
            }
            
            RedisURI redisUri = builder.build();
            
            // Create client and connect
            redisClient = RedisClient.create(redisUri);
            connection = redisClient.connect();
            RedisCommands<String, String> commands = connection.sync();
            
            // Test PING
            String pong = commands.ping();
            System.out.println("✓ PING: " + pong);
            
            // Test SET/GET
            commands.set("test:diagnostic", "hello");
            String value = commands.get("test:diagnostic");
            System.out.println("✓ SET/GET: " + value);
            
            // Cleanup
            commands.del("test:diagnostic");
            
            System.out.println("✓ SUCCESS: Connection works!");
            
        } catch (Exception e) {
            System.err.println("✗ FAILED: " + e.getClass().getSimpleName() + ": " + e.getMessage());
            if (e.getCause() != null) {
                System.err.println("  Cause: " + e.getCause().getMessage());
            }
        } finally {
            if (connection != null) {
                try {
                    connection.close();
                } catch (Exception e) {
                    // Ignore
                }
            }
            if (redisClient != null) {
                try {
                    redisClient.shutdown();
                } catch (Exception e) {
                    // Ignore
                }
            }
        }
    }
}
