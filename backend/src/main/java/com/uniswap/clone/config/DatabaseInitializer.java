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
            
            // Create deposits table
            jdbcTemplate.execute("CREATE TABLE IF NOT EXISTS deposits (" +
                "id BIGINT AUTO_INCREMENT PRIMARY KEY, " +
                "user_id VARCHAR(64) NOT NULL, " +
                "currency VARCHAR(16) NOT NULL, " +
                "amount DECIMAL(20, 8) NOT NULL, " +
                "timestamp BIGINT NOT NULL, " +
                "INDEX idx_user_id (user_id), " +
                "INDEX idx_timestamp (timestamp)" +
                ")");
            System.out.println("Table 'deposits' check/creation completed.");
            
            // Create undo_log table for Seata
            // specific logic to ensure it runs on ALL datasources if using ShardingSphere
            javax.sql.DataSource dataSource = jdbcTemplate.getDataSource();
            if (dataSource != null && dataSource.getClass().getName().startsWith("org.apache.shardingsphere")) {
                 try {
                    java.lang.reflect.Method getDataSourceMapMethod = dataSource.getClass().getMethod("getDataSourceMap");
                    java.util.Map<String, javax.sql.DataSource> dataSourceMap = (java.util.Map<String, javax.sql.DataSource>) getDataSourceMapMethod.invoke(dataSource);
                    
                    String createUndoLogSql = "CREATE TABLE IF NOT EXISTS `undo_log` (" +
                        "`id` bigint(20) NOT NULL AUTO_INCREMENT, " +
                        "`branch_id` bigint(20) NOT NULL, " +
                        "`xid` varchar(100) NOT NULL, " +
                        "`context` varchar(128) NOT NULL, " +
                        "`rollback_info` longblob NOT NULL, " +
                        "`log_status` int(11) NOT NULL, " +
                        "`log_created` datetime NOT NULL, " +
                        "`log_modified` datetime NOT NULL, " +
                        "PRIMARY KEY (`id`), " +
                        "UNIQUE KEY `ux_undo_log` (`xid`,`branch_id`) " +
                        ") ENGINE=InnoDB AUTO_INCREMENT=1 DEFAULT CHARSET=utf8mb4";

                    for (java.util.Map.Entry<String, javax.sql.DataSource> entry : dataSourceMap.entrySet()) {
                        System.out.println("Initializing undo_log for datasource: " + entry.getKey());
                        try (java.sql.Connection conn = entry.getValue().getConnection();
                             java.sql.Statement stmt = conn.createStatement()) {
                            stmt.execute(createUndoLogSql);
                        }
                    }
                    System.out.println("Table 'undo_log' check/creation completed on all shards.");
                 } catch (Exception e) {
                     System.err.println("Failed to initialize undo_log on shards: " + e.getMessage());
                     // Fallback to simpler execution if reflection fails
                     jdbcTemplate.execute("CREATE TABLE IF NOT EXISTS undo_log (" +
                        "id BIGINT AUTO_INCREMENT PRIMARY KEY, " +
                        "branch_id BIGINT NOT NULL, " +
                        "xid VARCHAR(100) NOT NULL, " +
                        "rollback_info LONGBLOB, " +
                        "log_status INT NOT NULL, " +
                        "log_created DATETIME, " +
                        "log_modified DATETIME" +
                        ")");
                 }
            }

            System.out.println("Database initialization finished.");
        } catch (Exception e) {
            System.err.println("Error initializing database: " + e.getMessage());
            e.printStackTrace();
        }
    }
}
