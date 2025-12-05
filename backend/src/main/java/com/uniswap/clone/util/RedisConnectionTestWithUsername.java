package com.uniswap.clone.util;

import io.lettuce.core.RedisClient;
import io.lettuce.core.RedisURI;
import io.lettuce.core.api.StatefulRedisConnection;
import io.lettuce.core.api.sync.RedisCommands;

/**
 * Redis connection diagnostic tool - Test with username
 */
public class RedisConnectionTestWithUsername {
    
    public static void main(String[] args) {
        String host = "194.164.194.118";
        int port = 9502;
        String password = "pass123editmelol";
        
        System.out.println("========== Redis Connection Test with Username ==========");
        
        // Test with different usernames
        String[] usernames = {"default", "", "admin", "root"};
        
        for (String username : usernames) {
            testConnection(host, port, username, password);
        }
    }
    
    private static void testConnection(String host, int port, String username, String password) {
        String displayUsername = username.isEmpty() ? "(empty)" : username;
        System.out.println("\n----------------------------------------");
        System.out.println("Testing with username: " + displayUsername);
        System.out.println("----------------------------------------");
        
        RedisClient redisClient = null;
        StatefulRedisConnection<String, String> connection = null;
        
        try {
            // Build Redis URI with username
            RedisURI.Builder builder = RedisURI.builder()
                .withHost(host)
                .withPort(port);
            
            if (username != null && !username.isEmpty()) {
                builder.withAuthentication(username, password);
            } else {
                builder.withPassword(password.toCharArray());
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
            
            System.out.println("✓✓✓ SUCCESS! This combination works! ✓✓✓");
            System.out.println("Username: " + displayUsername);
            System.out.println("Password: ***");
            
        } catch (Exception e) {
            System.err.println("✗ FAILED: " + e.getClass().getSimpleName());
            System.err.println("  Message: " + e.getMessage());
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
