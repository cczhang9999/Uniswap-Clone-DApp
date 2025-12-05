package com.uniswap.clone.util;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.Statement;

/**
 * Verify database tables exist
 */
public class VerifyDatabaseTables {
    
    public static void main(String[] args) {
        String jdbcUrl = "jdbc:mysql://212.227.166.131:9257/myapp?useSSL=false&serverTimezone=UTC&allowPublicKeyRetrieval=true";
        String username = "hobart";
        String password = "123456";
        
        System.out.println("========== Verifying Database Tables ==========");
        
        try {
            Class.forName("com.mysql.cj.jdbc.Driver");
            
            try (Connection conn = DriverManager.getConnection(jdbcUrl, username, password)) {
                System.out.println("✓ Connected to database: " + jdbcUrl);
                System.out.println();
                
                // Show all tables
                try (Statement stmt = conn.createStatement()) {
                    ResultSet rs = stmt.executeQuery("SHOW TABLES");
                    
                    System.out.println("Tables in database:");
                    System.out.println("-------------------");
                    while (rs.next()) {
                        String tableName = rs.getString(1);
                        System.out.println("  - " + tableName);
                    }
                    rs.close();
                }
                
                System.out.println();
                
                // Check specific tables
                String[] requiredTables = {"users", "users_0", "users_1", "users_2", "deposits", "balances"};
                
                System.out.println("Checking required tables:");
                System.out.println("-------------------------");
                
                for (String table : requiredTables) {
                    try (Statement stmt = conn.createStatement()) {
                        ResultSet rs = stmt.executeQuery("SELECT COUNT(*) FROM " + table);
                        if (rs.next()) {
                            int count = rs.getInt(1);
                            System.out.println("  ✓ " + table + " (rows: " + count + ")");
                        }
                        rs.close();
                    } catch (Exception e) {
                        System.out.println("  ✗ " + table + " - " + e.getMessage());
                    }
                }
                
                System.out.println();
                System.out.println("==========================================");
                System.out.println("✓ Verification complete");
                
            }
            
        } catch (Exception e) {
            System.err.println("❌ Error:");
            e.printStackTrace();
        }
    }
}
