package com.uniswap.clone.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.math.BigDecimal;

@Data
@TableName("deposits")
public class Deposit {
    @TableId(type = IdType.AUTO)
    private Long id;
    private String userId;
    private String currency;
    private BigDecimal amount;
    private Long timestamp;
}
