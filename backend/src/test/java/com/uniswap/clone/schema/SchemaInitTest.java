package com.uniswap.clone.schema;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.jdbc.core.JdbcTemplate;

import javax.sql.DataSource;

@SpringBootTest
public class SchemaInitTest {

    @Autowired
    private DataSource dataSource;

    @Test
    public void createShardedTables() {
        JdbcTemplate jdbcTemplate = new JdbcTemplate(dataSource);
        
        String createTableSql0 = """
            CREATE TABLE IF NOT EXISTS users_0 (
                id BIGINT PRIMARY KEY,
                name VARCHAR(255),
                email VARCHAR(255)
            ) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;
        """;
        
        String createTableSql1 = """
            CREATE TABLE IF NOT EXISTS users_1 (
                id BIGINT PRIMARY KEY,
                name VARCHAR(255),
                email VARCHAR(255)
            ) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;
        """;

        jdbcTemplate.execute(createTableSql0);
        jdbcTemplate.execute(createTableSql1);
        System.out.println("Tables users_0 and users_1 created successfully.");
    }
}
