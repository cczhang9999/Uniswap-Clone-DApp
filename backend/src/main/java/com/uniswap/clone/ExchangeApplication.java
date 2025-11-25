package com.uniswap.clone;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import com.uniswap.clone.mapper.UserMapper;
import com.uniswap.clone.entity.User;

@SpringBootApplication
@MapperScan("com.uniswap.clone.mapper")
public class ExchangeApplication {

    public static void main(String[] args) {
        SpringApplication.run(ExchangeApplication.class, args);
    }
}
