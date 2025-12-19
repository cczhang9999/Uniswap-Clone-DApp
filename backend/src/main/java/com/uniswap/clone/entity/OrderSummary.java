package com.uniswap.clone.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import java.math.BigDecimal;

@Data
@TableName("orders_summary")
public class OrderSummary {
    @TableId(type = IdType.AUTO)
    private Long id;
    private String orderId;
    private String userId;
    private String symbol;
    private String side;
    private String type;
    private BigDecimal price;
    private BigDecimal quantity;
    private String status;
    private BigDecimal filledQuantity;
    private Long timestamp;
}
