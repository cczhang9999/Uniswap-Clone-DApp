package com.uniswap.clone.fallback;

import com.uniswap.clone.common.Result;
import com.uniswap.clone.model.Trade;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.util.Collections;
import java.util.List;

@Component
@Slf4j
public class OrderFallback {

    public static Result<List<Trade>> createOrderFallback(String userId, String symbol, String side, String type, BigDecimal price, BigDecimal quantity, Throwable t) {
        log.error("Order creation fallback triggered for user {}. Exception: ", userId, t);
        return Result.error(500, "System is busy, please try again later. (Fallback)");
    }
}
