package com.nuit.yujin.smartcateringbackend.vo;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class TableScanVO {
    private Long storeId;
    private Long tableId;
    private String tableNo;
    private String status;
}
