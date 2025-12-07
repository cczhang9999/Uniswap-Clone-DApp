package com.uniswap.clone.config;

import org.springframework.boot.CommandLineRunner;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;

@Component
public class DatabaseInitializer implements CommandLineRunner {

    private final JdbcTemplate jdbcTemplate;

    public DatabaseInitializer(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    @Override
    public void run(String... args) throws Exception {
        System.out.println("Initializing database tables...");
        try {
            jdbcTemplate.execute("CREATE TABLE IF NOT EXISTS orders (" +
                    "id BIGINT AUTO_INCREMENT PRIMARY KEY, " +
                    "order_id VARCHAR(255) NOT NULL, " +
                    "user_id VARCHAR(255), " +
                    "symbol VARCHAR(20), " +
                    "side VARCHAR(10), " +
                    "type VARCHAR(10), " +
                    "price DECIMAL(20, 8), " +
                    "quantity DECIMAL(20, 8), " +
                    "status VARCHAR(20), " +
                    "filled_quantity DECIMAL(20, 8) DEFAULT 0, " +
                    "timestamp BIGINT" +
                    ")");
            System.out.println("Table 'orders' check/creation completed.");
        } catch (Exception e) {
            System.err.println("Error initializing database: " + e.getMessage());
            e.printStackTrace();
        }
    }
}
