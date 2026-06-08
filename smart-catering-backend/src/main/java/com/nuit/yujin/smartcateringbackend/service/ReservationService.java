package com.nuit.yujin.smartcateringbackend.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.nuit.yujin.smartcateringbackend.dto.CreateReservationDTO;
import com.nuit.yujin.smartcateringbackend.entity.DiningTable;
import com.nuit.yujin.smartcateringbackend.entity.Reservation;
import com.nuit.yujin.smartcateringbackend.enums.TableStatus;
import com.nuit.yujin.smartcateringbackend.mapper.ReservationMapper;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Set;

@Service
public class ReservationService extends ServiceImpl<ReservationMapper, Reservation> {

    private static final String PENDING = "PENDING";
    private static final String CONFIRMED = "CONFIRMED";
    private static final String CANCELLED = "CANCELLED";
    private static final Set<String> MERCHANT_STATUSES = Set.of(PENDING, CONFIRMED, CANCELLED);

    private final DiningTableService diningTableService;

    public ReservationService(DiningTableService diningTableService) {
        this.diningTableService = diningTableService;
    }

    @Transactional
    public Reservation create(Long userId, CreateReservationDTO dto) {
        if (dto.getStoreId() == null) {
            dto.setStoreId(1L);
        }
        validateCreate(dto);
        diningTableService.requireAvailableForReservation(dto.getStoreId(), dto.getTableId());
        DiningTable table = diningTableService.requireStoreTable(dto.getStoreId(), dto.getTableId());

        Reservation reservation = new Reservation();
        reservation.setStoreId(dto.getStoreId());
        reservation.setUserId(userId);
        reservation.setTableId(dto.getTableId());
        reservation.setTableNo(table.getTableNo());
        reservation.setContactName(dto.getContactName());
        reservation.setContactPhone(dto.getContactPhone());
        reservation.setReservationDate(dto.getReservationDate());
        reservation.setReservationTime(dto.getReservationTime());
        reservation.setPartySize(dto.getPartySize());
        reservation.setStatus(PENDING);
        reservation.setRemark(dto.getRemark());
        reservation.setCreateTime(LocalDateTime.now());
        reservation.setUpdateTime(LocalDateTime.now());
        save(reservation);

        diningTableService.markReserved(dto.getStoreId(), dto.getTableId());
        return reservation;
    }

    public IPage<Reservation> pageUserReservations(Long userId, Integer pageNo, Integer pageSize) {
        return lambdaQuery()
                .eq(Reservation::getUserId, userId)
                .orderByDesc(Reservation::getCreateTime)
                .page(new Page<>(pageNo, pageSize));
    }

    public IPage<Reservation> pageMerchantReservations(Long storeId, String status, Integer pageNo, Integer pageSize) {
        return lambdaQuery()
                .eq(Reservation::getStoreId, storeId)
                .eq(status != null && !"ALL".equalsIgnoreCase(status), Reservation::getStatus, status)
                .orderByDesc(Reservation::getCreateTime)
                .page(new Page<>(pageNo, pageSize));
    }

    @Transactional
    public void cancelUserReservation(Long userId, Long reservationId) {
        Reservation reservation = requireUserReservation(userId, reservationId);
        cancelReservation(reservation);
    }

    @Transactional
    public Reservation updateMerchantStatus(Long storeId, Long reservationId, String status) {
        String normalized = normalizeStatus(status);
        Reservation reservation = requireStoreReservation(storeId, reservationId);
        if (CANCELLED.equals(normalized)) {
            cancelReservation(reservation);
            return reservation;
        }
        reservation.setStatus(normalized);
        reservation.setUpdateTime(LocalDateTime.now());
        updateById(reservation);
        return reservation;
    }

    private void cancelReservation(Reservation reservation) {
        reservation.setStatus(CANCELLED);
        reservation.setUpdateTime(LocalDateTime.now());
        updateById(reservation);
        diningTableService.markFree(reservation.getStoreId(), reservation.getTableId());
    }

    private Reservation requireUserReservation(Long userId, Long reservationId) {
        Reservation reservation = getById(reservationId);
        if (reservation == null || !reservation.getUserId().equals(userId)) {
            throw new RuntimeException("预约不存在");
        }
        return reservation;
    }

    private Reservation requireStoreReservation(Long storeId, Long reservationId) {
        Reservation reservation = getById(reservationId);
        if (reservation == null || !reservation.getStoreId().equals(storeId)) {
            throw new RuntimeException("预约不存在");
        }
        return reservation;
    }

    private void validateCreate(CreateReservationDTO dto) {
        if (dto.getTableId() == null) {
            throw new RuntimeException("请选择预约桌台");
        }
        if (dto.getReservationDate() == null || dto.getReservationTime() == null) {
            throw new RuntimeException("请选择预约日期和时间");
        }
        if (dto.getPartySize() == null || dto.getPartySize() <= 0) {
            throw new RuntimeException("请输入正确的就餐人数");
        }
        if (dto.getContactName() == null || dto.getContactName().isBlank()) {
            throw new RuntimeException("请输入联系人");
        }
        if (dto.getContactPhone() == null || dto.getContactPhone().isBlank()) {
            throw new RuntimeException("请输入联系电话");
        }
    }

    private String normalizeStatus(String status) {
        if (status == null || status.isBlank()) {
            throw new RuntimeException("预约状态不能为空");
        }
        String normalized = status.toUpperCase();
        if (!MERCHANT_STATUSES.contains(normalized)) {
            throw new RuntimeException("不支持的预约状态：" + status);
        }
        return normalized;
    }
}
