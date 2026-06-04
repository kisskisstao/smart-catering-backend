package com.nuit.yujin.smartcateringbackend.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@TableName("dish_category")
public class DishCategory {
    @TableId(type = IdType.AUTO)
    private Long id;
    private Long storeId;
    private String name;
    private Integer sort;
    private String status;
    private LocalDateTime createTime;
    private LocalDateTime updateTime;
}
