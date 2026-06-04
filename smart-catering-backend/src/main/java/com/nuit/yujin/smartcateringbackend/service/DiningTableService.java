package com.nuit.yujin.smartcateringbackend.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.nuit.yujin.smartcateringbackend.entity.DiningTable;
import com.nuit.yujin.smartcateringbackend.enums.TableStatus;
import com.nuit.yujin.smartcateringbackend.mapper.DiningTableMapper;
import com.nuit.yujin.smartcateringbackend.vo.TableScanVO;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.UUID;

@Service
public class DiningTableService extends ServiceImpl<DiningTableMapper, DiningTable> {

    private final RedisService redisService;

    public DiningTableService(RedisService redisService) {
        this.redisService = redisService;
    }

    public IPage<DiningTable> pageByStore(Long storeId, Integer pageNo, Integer pageSize) {
        return lambdaQuery()
                .eq(DiningTable::getStoreId, storeId)
                .orderByAsc(DiningTable::getTableNo)
                .page(new Page<>(pageNo, pageSize));
    }

    public void createTable(Long storeId, DiningTable table) {
        table.setStoreId(storeId);
        table.setStatus(table.getStatus() == null ? TableStatus.AVAILABLE.name() : table.getStatus());
        table.setSeats(table.getSeats() == null ? 4 : table.getSeats());
        table.setCreateTime(LocalDateTime.now());
        table.setUpdateTime(LocalDateTime.now());
        save(table);
    }

    public void updateTable(Long storeId, Long id, DiningTable updateData) {
        DiningTable table = requireStoreTable(storeId, id);
        table.setTableNo(updateData.getTableNo());
        table.setSeats(updateData.getSeats());
        table.setStatus(updateData.getStatus());
        table.setUpdateTime(LocalDateTime.now());
        updateById(table);
        redisService.set(redisService.tableStatusKey(id), table.getStatus(), Duration.ofMinutes(1));
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
        if (qrToken == null || !qrToken.equals(table.getQrToken())) {
            throw new RuntimeException("二维码无效或已失效");
        }
        if (TableStatus.DISABLED.name().equals(table.getStatus())) {
            throw new RuntimeException("桌台已停用");
        }
        redisService.set(redisService.tableStatusKey(tableId), table.getStatus(), Duration.ofMinutes(1));
        return new TableScanVO(table.getStoreId(), table.getId(), table.getTableNo(), table.getStatus());
    }

    public DiningTable requireUsableTable(Long tableId) {
        DiningTable table = getById(tableId);
        if (table == null || TableStatus.DISABLED.name().equals(table.getStatus())) {
            throw new RuntimeException("桌台不可用");
        }
        return table;
    }

    private DiningTable requireStoreTable(Long storeId, Long tableId) {
        DiningTable table = getById(tableId);
        if (table == null || !table.getStoreId().equals(storeId)) {
            throw new RuntimeException("桌台不存在");
        }
        return table;
    }
}
