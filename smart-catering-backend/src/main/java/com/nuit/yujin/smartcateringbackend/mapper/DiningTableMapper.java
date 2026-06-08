package com.nuit.yujin.smartcateringbackend.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.nuit.yujin.smartcateringbackend.entity.DiningTable;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

@Mapper
public interface DiningTableMapper extends BaseMapper<DiningTable> {
    @Select("SELECT * FROM dining_table WHERE id = #{id} FOR UPDATE")
    DiningTable selectByIdForUpdate(@Param("id") Long id);
}
