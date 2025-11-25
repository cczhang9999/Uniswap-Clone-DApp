package com.uniswap.clone.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.uniswap.clone.entity.User;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface UserMapper extends BaseMapper<User> {
}
