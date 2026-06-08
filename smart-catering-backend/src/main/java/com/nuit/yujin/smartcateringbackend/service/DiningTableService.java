package com.nuit.yujin.smartcateringbackend.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.nuit.yujin.smartcateringbackend.entity.DiningTable;
import com.nuit.yujin.smartcateringbackend.enums.TableStatus;
import com.nuit.yujin.smartcateringbackend.mapper.DiningTableMapper;
import com.nuit.yujin.smartcateringbackend.vo.TableScanVO;
import com.nuit.yujin.smartcateringbackend.websocket.TableStatusWebSocketHandler;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;

@Service
public class DiningTableService extends ServiceImpl<DiningTableMapper, DiningTable> {

    private final RedisService redisService;
    private final TableStatusWebSocketHandler tableStatusWebSocketHandler;

    public DiningTableService(RedisService redisService,
                              TableStatusWebSocketHandler tableStatusWebSocketHandler) {
        this.redisService = redisService;
        this.tableStatusWebSocketHandler = tableStatusWebSocketHandler;
    }

    public IPage<DiningTable> pageByStore(Long storeId, Integer pageNo, Integer pageSize) {
        return lambdaQuery()
                .eq(DiningTable::getStoreId, storeId)
                .orderByAsc(DiningTable::getTableNo)
                .page(new Page<>(pageNo, pageSize));
    }

    public List<DiningTable> listByStore(Long storeId) {
        List<DiningTable> tables = lambdaQuery()
                .eq(DiningTable::getStoreId, storeId)
                .orderByAsc(DiningTable::getTableNo)
                .list();
        tables.forEach(this::normalizeTableStatusInMemory);
        return tables;
    }

    public void createTable(Long storeId, DiningTable table) {
        table.setStoreId(storeId);
        table.setStatus(table.getStatus() == null ? TableStatus.FREE.name() : parseStatus(table.getStatus()).name());
        table.setSeats(table.getSeats() == null ? 4 : table.getSeats());
        table.setCreateTime(LocalDateTime.now());
        table.setUpdateTime(LocalDateTime.now());
        save(table);
        syncTableStatus(table);
    }

    public void updateTable(Long storeId, Long id, DiningTable updateData) {
        DiningTable table = requireStoreTable(storeId, id);
        table.setTableNo(updateData.getTableNo());
        table.setSeats(updateData.getSeats());
        if (updateData.getStatus() != null) {
            table.setStatus(parseStatus(updateData.getStatus()).name());
        }
        table.setUpdateTime(LocalDateTime.now());
        updateById(table);
        syncTableStatus(table);
    }

    public DiningTable updateStatus(Long storeId, Long id, String status) {
        DiningTable table = requireStoreTable(storeId, id);
        table.setStatus(parseStatus(status).name());
        table.setUpdateTime(LocalDateTime.now());
        updateById(table);
        syncTableStatus(table);
        return table;
    }

    public void markFree(Long storeId, Long tableId) {
        updateStatus(storeId, tableId, TableStatus.FREE.name());
    }

    public void markReserved(Long storeId, Long tableId) {
        updateStatus(storeId, tableId, TableStatus.RESERVED.name());
    }

    public void markOccupied(Long storeId, Long tableId) {
        updateStatus(storeId, tableId, TableStatus.OCCUPIED.name());
    }

    public void markDirty(Long storeId, Long tableId) {
        updateStatus(storeId, tableId, TableStatus.DIRTY.name());
    }

    public void deleteTable(Long storeId, Long id) {
        requireStoreTable(storeId, id);
        removeById(id);
        redisService.delete(redisService.tableStatusKey(id));
    }

    public DiningTable refreshQrCode(Long storeId, Long tableId) {
        DiningTable table = requireStoreTable(storeId, tableId);
        String qrToken = UUID.randomUUID().toString();
        String qrContent = "/pages/menu/menu?storeId=" + storeId
                + "&tableId=" + tableId
                + "&qrToken=" + qrToken;
        table.setQrToken(qrToken);
        table.setQrContent(qrContent);
        table.setQrUpdateTime(LocalDateTime.now());
        table.setUpdateTime(LocalDateTime.now());
        updateById(table);
        return table;
    }

    public TableScanVO scan(Long tableId, String qrToken) {
        DiningTable table = getById(tableId);
        if (table == null) {
            throw new RuntimeException("桌台不存在");
        }
        normalizeTableStatusInMemory(table);
        if (qrToken == null || !qrToken.equals(table.getQrToken())) {
            throw new RuntimeException("二维码无效或已失效");
        }
        redisService.set(redisService.tableStatusKey(tableId), table.getStatus(), Duration.ofMinutes(1));
        return new TableScanVO(table.getStoreId(), table.getId(), table.getTableNo(), table.getStatus());
    }

    public DiningTable requireFreeTableForOrder(Long tableId) {
        DiningTable table = getById(tableId);
        if (table == null) {
            throw new RuntimeException("桌台不存在");
        }
        normalizeTableStatusInMemory(table);
        if (!TableStatus.FREE.name().equals(table.getStatus())) {
            throw new RuntimeException("当前桌台不可点单，请选择空闲桌台");
        }
        return table;
    }

    public DiningTable requireOrderableTableWithLock(Long tableId) {
        DiningTable table = baseMapper.selectByIdForUpdate(tableId);
        if (table == null) {
            throw new RuntimeException("桌台不存在");
        }
        normalizeTableStatusInMemory(table);
        if (!TableStatus.FREE.name().equals(table.getStatus())
                && !TableStatus.OCCUPIED.name().equals(table.getStatus())) {
            throw new RuntimeException("当前桌台不可点单");
        }
        return table;
    }

    public DiningTable requireStoreTable(Long storeId, Long tableId) {
        DiningTable table = getById(tableId);
        if (table == null || !table.getStoreId().equals(storeId)) {
            throw new RuntimeException("桌台不存在");
        }
        normalizeTableStatusInMemory(table);
        return table;
    }

    public void requireAvailableForReservation(Long storeId, Long tableId) {
        DiningTable table = requireStoreTable(storeId, tableId);
        if (!TableStatus.FREE.name().equals(table.getStatus())) {
            throw new RuntimeException("只能预约空闲桌台");
        }
    }

    private TableStatus parseStatus(String status) {
        if (status == null || status.isBlank()) {
            throw new RuntimeException("桌台状态不能为空");
        }
        String normalized = normalizeStatusValue(status);
        return Arrays.stream(TableStatus.values())
                .filter(item -> item.name().equalsIgnoreCase(normalized))
                .findFirst()
                .orElseThrow(() -> new RuntimeException("不支持的桌台状态：" + status));
    }

    private void normalizeTableStatusInMemory(DiningTable table) {
        table.setStatus(parseStatus(table.getStatus()).name());
    }

    private String normalizeStatusValue(String status) {
        return switch (status.toUpperCase()) {
            case "AVAILABLE" -> TableStatus.FREE.name();
            case "USING" -> TableStatus.OCCUPIED.name();
            case "CLEANING" -> TableStatus.DIRTY.name();
            case "DISABLED" -> TableStatus.DIRTY.name();
            default -> status.toUpperCase();
        };
    }

    private void syncTableStatus(DiningTable table) {
        redisService.set(redisService.tableStatusKey(table.getId()), table.getStatus(), Duration.ofMinutes(1));
        tableStatusWebSocketHandler.pushTableStatusChanged(table);
    }
}
