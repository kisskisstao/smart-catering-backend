package com.nuit.yujin.smartcateringbackend.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@TableName("dining_table")
public class DiningTable {
    @TableId(type = IdType.AUTO)
    private Long id;
    private Long storeId;
    private String tableNo;
    private Integer seats;
    private String status;
    private String qrToken;
    private String qrContent;
    private LocalDateTime qrUpdateTime;
    private LocalDateTime createTime;
    private LocalDateTime updateTime;
}
