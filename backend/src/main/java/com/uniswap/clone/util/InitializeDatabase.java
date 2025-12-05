package com.uniswap.clone.util;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.Statement;
import java.util.stream.Collectors;

/**
 * Initialize database schema
 */
public class InitializeDatabase {
    
    public static void main(String[] args) {
        String jdbcUrl = "jdbc:mysql://212.227.166.131:9257/myapp?useSSL=false&serverTimezone=UTC&allowPublicKeyRetrieval=true";
        String username = "hobart";
        String password = "123456";
        
        System.out.println("========== Initializing Database ==========");
        System.out.println("JDBC URL: " + jdbcUrl);
        System.out.println("Username: " + username);
        System.out.println("==========================================\n");
        
        try {
            // Load schema.sql from resources
            InputStream is = InitializeDatabase.class.getClassLoader().getResourceAsStream("schema.sql");
            if (is == null) {
                System.err.println("❌ schema.sql not found in resources");
                return;
            }
            
            String sql = new BufferedReader(new InputStreamReader(is))
                .lines()
                .collect(Collectors.joining("\n"));
            
            System.out.println("✓ Loaded schema.sql");
            System.out.println("SQL content:\n" + sql);
            System.out.println("\n==========================================\n");
            
            // Load MySQL driver
            Class.forName("com.mysql.cj.jdbc.Driver");
            System.out.println("✓ Loaded MySQL driver");
            
            // Connect to database
            try (Connection conn = DriverManager.getConnection(jdbcUrl, username, password)) {
                System.out.println("✓ Connected to database");
                
                // Split SQL by semicolon and execute each statement
                String[] statements = sql.split(";");
                int successCount = 0;
                
                for (String statement : statements) {
                    statement = statement.trim();
                    if (statement.isEmpty()) {
                        continue;
                    }
                    
                    try (Statement stmt = conn.createStatement()) {
                        stmt.execute(statement);
                        successCount++;
                        
                        // Extract table name from CREATE TABLE statement
                        String tableName = "unknown";
                        if (statement.toUpperCase().contains("CREATE TABLE")) {
                            int start = statement.toUpperCase().indexOf("TABLE") + 5;
                            int end = statement.indexOf("(", start);
                            if (end > start) {
                                tableName = statement.substring(start, end).trim()
                                    .replace("IF NOT EXISTS", "").trim();
                            }
                        }
                        
                        System.out.println("✓ Created table: " + tableName);
                    } catch (Exception e) {
                        System.err.println("⚠ Warning executing statement: " + e.getMessage());
                        // Continue with next statement
                    }
                }
                
                System.out.println("\n==========================================");
                System.out.println("✓✓✓ Database initialization complete!");
                System.out.println("Successfully executed " + successCount + " statements");
                System.out.println("==========================================");
                
            }
            
        } catch (Exception e) {
            System.err.println("❌ Error initializing database:");
            e.printStackTrace();
        }
    }
}
