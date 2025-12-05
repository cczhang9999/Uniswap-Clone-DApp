package com.uniswap.clone.schema;

import org.junit.jupiter.api.Test;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.Statement;

public class PhysicalTableCreatorTest {

    @Test
    public void createTablesDirectly() throws Exception {
        String url = "jdbc:mysql://212.227.166.131:9257/myapp?useSSL=false&serverTimezone=UTC&allowPublicKeyRetrieval=true";
        String username = "hobart";
        String password = "123456";

        try (Connection conn = DriverManager.getConnection(url, username, password);
             Statement stmt = conn.createStatement()) {
            
            // Drop existing tables first to ensure clean state
            String[] dropSqls = {
                "DROP TABLE IF EXISTS users_0",
                "DROP TABLE IF EXISTS users_1",
                "DROP TABLE IF EXISTS users_2"
            };
            
            for (String sql : dropSqls) {
                stmt.execute(sql);
                System.out.println("Executed: " + sql);
            }
            
            // Create tables with explicit collation
            String[] createSqls = {
                """
                CREATE TABLE users_0 (
                    id BIGINT PRIMARY KEY,
                    name VARCHAR(255) COLLATE utf8mb4_unicode_ci,
                    email VARCHAR(255) COLLATE utf8mb4_unicode_ci
                ) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
                """,
                """
                CREATE TABLE users_1 (
                    id BIGINT PRIMARY KEY,
                    name VARCHAR(255) COLLATE utf8mb4_unicode_ci,
                    email VARCHAR(255) COLLATE utf8mb4_unicode_ci
                ) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
                """,
                """
                CREATE TABLE users_2 (
                    id BIGINT PRIMARY KEY,
                    name VARCHAR(255) COLLATE utf8mb4_unicode_ci,
                    email VARCHAR(255) COLLATE utf8mb4_unicode_ci
                ) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
                """
            };

            for (String sql : createSqls) {
                stmt.execute(sql);
                System.out.println("Created table with consistent collation");
            }
            System.out.println("All physical tables created successfully with utf8mb4_unicode_ci collation.");
        }
    }
}
