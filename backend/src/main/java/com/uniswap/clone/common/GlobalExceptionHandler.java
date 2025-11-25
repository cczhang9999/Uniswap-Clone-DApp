package com.uniswap.clone.common;

import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(Exception.class)
    public Result<String> handleException(Exception e) {
        return Result.error(500, e.getMessage());
    }
    
    // You can add more specific exception handlers here
    // @ExceptionHandler(MyCustomException.class) ...
}
