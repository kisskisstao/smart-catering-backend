package com.nuit.yujin.smartcateringbackend.controller;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.nuit.yujin.smartcateringbackend.common.Result;
import com.nuit.yujin.smartcateringbackend.dto.CreateReservationDTO;
import com.nuit.yujin.smartcateringbackend.entity.Reservation;
import com.nuit.yujin.smartcateringbackend.service.ReservationService;
import com.nuit.yujin.smartcateringbackend.utils.JwtUtils;
import org.springframework.web.bind.annotation.*;

@RestController
public class ReservationController {

    private final ReservationService reservationService;
    private final JwtUtils jwtUtils;

    public ReservationController(ReservationService reservationService, JwtUtils jwtUtils) {
        this.reservationService = reservationService;
        this.jwtUtils = jwtUtils;
    }

    @PostMapping("/reservation/create")
    public Result<Reservation> create(@RequestHeader("Authorization") String token,
                                      @RequestBody CreateReservationDTO dto) {
        return Result.success(reservationService.create(jwtUtils.getUserId(token), dto));
    }

    @GetMapping("/reservation/my")
    public Result<IPage<Reservation>> my(@RequestHeader("Authorization") String token,
                                         @RequestParam(defaultValue = "1") Integer pageNo,
                                         @RequestParam(defaultValue = "10") Integer pageSize) {
        return Result.success(reservationService.pageUserReservations(jwtUtils.getUserId(token), pageNo, pageSize));
    }

    @PutMapping("/reservation/{id}/cancel")
    public Result<Void> cancel(@RequestHeader("Authorization") String token,
                               @PathVariable Long id) {
        reservationService.cancelUserReservation(jwtUtils.getUserId(token), id);
        return Result.success(null);
    }

    @GetMapping("/merchant/reservation/page")
    public Result<IPage<Reservation>> merchantPage(@RequestParam(defaultValue = "1") Long storeId,
                                                   @RequestParam(defaultValue = "ALL") String status,
                                                   @RequestParam(defaultValue = "1") Integer pageNo,
                                                   @RequestParam(defaultValue = "10") Integer pageSize) {
        return Result.success(reservationService.pageMerchantReservations(storeId, status, pageNo, pageSize));
    }

    @PutMapping("/merchant/reservation/{id}/status")
    public Result<Reservation> merchantStatus(@RequestParam(defaultValue = "1") Long storeId,
                                              @PathVariable Long id,
                                              @RequestParam String status) {
        return Result.success(reservationService.updateMerchantStatus(storeId, id, status));
    }
}
