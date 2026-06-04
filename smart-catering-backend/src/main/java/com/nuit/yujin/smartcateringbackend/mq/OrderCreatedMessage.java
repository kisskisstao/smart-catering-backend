package com.nuit.yujin.smartcateringbackend.mq;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.math.BigDecimal;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class OrderCreatedMessage implements Serializable {
    private Long orderId;
    private String orderNo;
    private Long storeId;
    private Long userId;
    private BigDecimal totalAmount;
}
