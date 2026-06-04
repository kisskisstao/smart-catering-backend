package com.nuit.yujin.smartcateringbackend.vo;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.math.BigDecimal;

@Data
@AllArgsConstructor
public class CreateOrderResultVO {
    private Long orderId;
    private String orderNo;
    private BigDecimal totalAmount;
    private String status;
    private String payStatus;
}
