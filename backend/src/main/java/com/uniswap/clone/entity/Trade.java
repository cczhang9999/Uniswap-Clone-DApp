package com.uniswap.clone.entity;

import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.math.BigDecimal;

@Data
@TableName("trade")
public class Trade {
    @TableId
    private String tradeId;
    private String buyerUserId;
    private String sellerUserId;
    private String symbol;
    private String buyOrderId;
    private String sellOrderId;
    private BigDecimal price;
    private BigDecimal quantity;
    private Long timestamp;
}
