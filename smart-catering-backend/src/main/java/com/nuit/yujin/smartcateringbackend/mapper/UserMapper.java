package com.nuit.yujin.smartcateringbackend.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.nuit.yujin.smartcateringbackend.entity.User;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface UserMapper extends BaseMapper<User> {
}