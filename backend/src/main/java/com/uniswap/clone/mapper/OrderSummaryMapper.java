package com.uniswap.clone.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.uniswap.clone.entity.OrderSummary;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface OrderSummaryMapper extends BaseMapper<OrderSummary> {
}
