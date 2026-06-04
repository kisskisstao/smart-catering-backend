package com.nuit.yujin.smartcateringbackend.dto;

import lombok.Data;

import java.util.List;

@Data
public class CreateOrderDTO {
    private Long tableId;
    private List<CreateOrderItemDTO> items;
}
