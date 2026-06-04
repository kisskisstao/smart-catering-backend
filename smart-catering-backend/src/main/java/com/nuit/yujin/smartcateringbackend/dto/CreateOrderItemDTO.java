package com.nuit.yujin.smartcateringbackend.dto;

import lombok.Data;

@Data
public class CreateOrderItemDTO {
    private Long dishId;
    private Integer quantity;
    private String spicy;
    private String size;
}
