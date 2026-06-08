package com.nuit.yujin.smartcateringbackend.dto;

import lombok.Data;

import java.time.LocalDate;
import java.time.LocalTime;

@Data
public class CreateReservationDTO {
    private Long storeId;
    private Long tableId;
    private String contactName;
    private String contactPhone;
    private LocalDate reservationDate;
    private LocalTime reservationTime;
    private Integer partySize;
    private String remark;
}
