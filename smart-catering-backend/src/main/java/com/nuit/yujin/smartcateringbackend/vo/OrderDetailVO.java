package com.nuit.yujin.smartcateringbackend.vo;

import com.nuit.yujin.smartcateringbackend.entity.DiningOrderItem;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Data
public class OrderDetailVO {
    private Long id;
    private String orderNo;
    private Long tableId;
    private String tableNo;
    private String status;
    private String statusText;
    private String payStatus;
    private BigDecimal totalAmount;
    private LocalDateTime createTime;
    private List<DiningOrderItem> items;
}
