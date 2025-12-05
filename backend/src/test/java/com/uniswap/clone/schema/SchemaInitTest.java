package com.uniswap.clone.schema;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.jdbc.core.JdbcTemplate;

import javax.sql.DataSource;

import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;

@SpringBootTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
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
        
        String createTableSql2 = """
            CREATE TABLE IF NOT EXISTS users_2 (
                id BIGINT PRIMARY KEY,
                name VARCHAR(255),
                email VARCHAR(255)
            ) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;
        """;

        jdbcTemplate.execute(createTableSql0);
        jdbcTemplate.execute(createTableSql1);
        jdbcTemplate.execute(createTableSql2);
        System.out.println("Tables users_0, users_1, and users_2 created successfully.");
    }
}
