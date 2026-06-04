package com.nuit.yujin.smartcateringbackend.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.nuit.yujin.smartcateringbackend.entity.Dish;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Update;

@Mapper
public interface DishMapper extends BaseMapper<Dish> {

    @Update("""
            UPDATE dish
            SET stock = stock - #{quantity},
                sales_count = sales_count + #{quantity},
                version = version + 1,
                update_time = NOW()
            WHERE id = #{dishId}
              AND stock >= #{quantity}
              AND version = #{version}
            """)
    int deductStockWithVersion(@Param("dishId") Long dishId,
                               @Param("quantity") Integer quantity,
                               @Param("version") Integer version);

    @Update("""
            UPDATE dish
            SET stock = stock + #{quantity},
                sales_count = GREATEST(sales_count - #{quantity}, 0),
                version = version + 1,
                update_time = NOW()
            WHERE id = #{dishId}
            """)
    int restoreStock(@Param("dishId") Long dishId,
                     @Param("quantity") Integer quantity);
}