package com.uniswap.clone.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.uniswap.clone.entity.Trade;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface TradeMapper extends BaseMapper<Trade> {
}
