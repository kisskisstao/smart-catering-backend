package com.nuit.yujin.smartcateringbackend.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Data
@TableName("dish")
public class Dish {
    @TableId(type = IdType.AUTO)
    private Long id;
    private Long storeId;
    private Long categoryId;
    private String name;
    private String imageUrl;
    private BigDecimal price;
    private String description;
    private Integer stock;
    private Integer salesCount;
    private String status;
    private String tasteTags;
    private String spicyOptions;
    private String sizeOptions;
    private Boolean recommended;
    private Integer recommendWeight;
    private Integer version;
    private LocalDateTime createTime;
    private LocalDateTime updateTime;

    @TableField(exist = false)
    private List<DishSpec> specs;
}
