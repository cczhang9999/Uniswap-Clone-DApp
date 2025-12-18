package com.uniswap.clone.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.math.BigDecimal;

@Data
@TableName("t_order")
public class Order {
    @TableId(value = "order_id", type = IdType.INPUT)
    private String orderId;
    private String userId;
    private String symbol;
    private String side; // BUY, SELL
    private String type; // LIMIT, MARKET
    private BigDecimal price;
    private BigDecimal quantity;
    private String status; // PENDING, PARTIAL, FILLED, CANCELED
    private BigDecimal filledQuantity;
    private Long timestamp;
}
