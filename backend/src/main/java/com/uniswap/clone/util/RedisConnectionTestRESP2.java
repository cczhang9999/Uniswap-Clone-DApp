package com.uniswap.clone.util;

import io.lettuce.core.RedisClient;
import io.lettuce.core.RedisURI;
import io.lettuce.core.api.StatefulRedisConnection;
import io.lettuce.core.api.sync.RedisCommands;
import io.lettuce.core.protocol.ProtocolVersion;

/**
 * Test Redis connection with RESP2 protocol
 */
public class RedisConnectionTestRESP2 {
    
    public static void main(String[] args) {
        String host = "194.164.194.118";
        int port = 9502;
        String password = "pass123editmelol";
        
        System.out.println("========== Testing with RESP2 Protocol ==========");
        
        RedisClient redisClient = null;
        StatefulRedisConnection<String, String> connection = null;
        
        try {
            // Build Redis URI with RESP2 protocol
            RedisURI redisUri = RedisURI.builder()
                .withHost(host)
                .withPort(port)
                .withPassword(password.toCharArray())
                .build();
            
            // Create client
            redisClient = RedisClient.create(redisUri);
            
            // Force RESP2 protocol
            redisClient.setOptions(io.lettuce.core.ClientOptions.builder()
                .protocolVersion(ProtocolVersion.RESP2)
                .build());
            
            // Connect
            connection = redisClient.connect();
            RedisCommands<String, String> commands = connection.sync();
            
            // Test PING
            String pong = commands.ping();
            System.out.println("✓ PING: " + pong);
            
            // Test SET/GET
            commands.set("test:resp2", "hello from RESP2");
            String value = commands.get("test:resp2");
            System.out.println("✓ SET/GET: " + value);
            
            // Test INFO
            String info = commands.info("server");
            System.out.println("✓ Server Info:");
            for (String line : info.split("\n")) {
                if (line.contains("redis_version") || line.contains("redis_mode")) {
                    System.out.println("  " + line.trim());
                }
            }
            
            // Cleanup
            commands.del("test:resp2");
            
            System.out.println("\n✓✓✓ SUCCESS! RESP2 protocol works! ✓✓✓");
            System.out.println("\nConfiguration to use:");
            System.out.println("  Host: " + host);
            System.out.println("  Port: " + port);
            System.out.println("  Password: ***");
            System.out.println("  Protocol: RESP2");
            
        } catch (Exception e) {
            System.err.println("\n✗ FAILED with RESP2");
            System.err.println("Error: " + e.getClass().getSimpleName());
            System.err.println("Message: " + e.getMessage());
            if (e.getCause() != null) {
                System.err.println("Cause: " + e.getCause().getMessage());
            }
            e.printStackTrace();
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
