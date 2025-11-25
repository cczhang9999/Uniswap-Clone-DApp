package com.uniswap.clone.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.uniswap.clone.entity.Balance;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface BalanceMapper extends BaseMapper<Balance> {
}
