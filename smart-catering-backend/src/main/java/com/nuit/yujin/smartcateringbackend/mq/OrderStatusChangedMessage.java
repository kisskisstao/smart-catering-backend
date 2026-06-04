package com.nuit.yujin.smartcateringbackend.mq;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class OrderStatusChangedMessage implements Serializable {
    private Long orderId;
    private Long storeId;
    private Long userId;
    private String status;
}
