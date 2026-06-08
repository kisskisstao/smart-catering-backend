package com.nuit.yujin.smartcateringbackend.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;

@Data
@TableName("reservation")
public class Reservation {
    @TableId(type = IdType.AUTO)
    private Long id;
    private Long storeId;
    private Long userId;
    private Long tableId;
    private String tableNo;
    private String contactName;
    private String contactPhone;
    private LocalDate reservationDate;
    private LocalTime reservationTime;
    private Integer partySize;
    private String status;
    private String remark;
    private LocalDateTime createTime;
    private LocalDateTime updateTime;
}
