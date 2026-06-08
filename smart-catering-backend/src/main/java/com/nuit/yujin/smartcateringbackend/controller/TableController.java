package com.nuit.yujin.smartcateringbackend.controller;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.nuit.yujin.smartcateringbackend.common.Result;
import com.nuit.yujin.smartcateringbackend.entity.DiningTable;
import com.nuit.yujin.smartcateringbackend.service.DiningTableService;
import com.nuit.yujin.smartcateringbackend.utils.QRCodeUtils;
import com.nuit.yujin.smartcateringbackend.vo.TableScanVO;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;
import java.util.List;

@RestController
public class TableController {

    private final DiningTableService diningTableService;

    public TableController(DiningTableService diningTableService) {
        this.diningTableService = diningTableService;
    }

    @GetMapping("/table/list")
    public Result<List<DiningTable>> list(@RequestParam(defaultValue = "1") Long storeId) {
        return Result.success(diningTableService.listByStore(storeId));
    }

    @PutMapping("/table/{id}/status")
    public Result<DiningTable> updateStatus(@RequestParam(defaultValue = "1") Long storeId,
                                            @PathVariable Long id,
                                            @RequestParam String status) {
        return Result.success(diningTableService.updateStatus(storeId, id, status));
    }

    @GetMapping("/merchant/table/page")
    public Result<IPage<DiningTable>> page(@RequestParam(defaultValue = "1") Long storeId,
                                           @RequestParam(defaultValue = "1") Integer pageNo,
                                           @RequestParam(defaultValue = "10") Integer pageSize) {
        return Result.success(diningTableService.pageByStore(storeId, pageNo, pageSize));
    }

    @PostMapping("/merchant/table")
    public Result<Void> create(@RequestParam(defaultValue = "1") Long storeId,
                               @RequestBody DiningTable table) {
        diningTableService.createTable(storeId, table);
        return Result.success(null);
    }

    @PutMapping("/merchant/table/{id}")
    public Result<Void> update(@RequestParam(defaultValue = "1") Long storeId,
                               @PathVariable Long id,
                               @RequestBody DiningTable table) {
        diningTableService.updateTable(storeId, id, table);
        return Result.success(null);
    }

    @DeleteMapping("/merchant/table/{id}")
    public Result<Void> delete(@RequestParam(defaultValue = "1") Long storeId,
                               @PathVariable Long id) {
        diningTableService.deleteTable(storeId, id);
        return Result.success(null);
    }

    @PostMapping("/merchant/table/{id}/qrcode")
    public Result<DiningTable> refreshQrCode(@RequestParam(defaultValue = "1") Long storeId,
                                             @PathVariable Long id) {
        return Result.success(diningTableService.refreshQrCode(storeId, id));
    }

    @GetMapping("/merchant/table/{id}/qrcode/image")
    public void qrCodeImage(@RequestParam(defaultValue = "1") Long storeId,
                            @PathVariable Long id,
                            HttpServletResponse response) throws IOException {
        DiningTable table = diningTableService.requireStoreTable(storeId, id);
        if (table.getQrContent() == null || table.getQrContent().isBlank()) {
            table = diningTableService.refreshQrCode(storeId, id);
        }
        byte[] bytes = QRCodeUtils.generatePng(table.getQrContent(), 300, 300);
        response.setContentType("image/png");
        response.getOutputStream().write(bytes);
    }

    @GetMapping("/table/scan")
    public Result<TableScanVO> scan(@RequestParam Long tableId,
                                    @RequestParam String qrToken) {
        return Result.success(diningTableService.scan(tableId, qrToken));
    }
}
