package com.nuit.yujin.smartcateringbackend.controller;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.nuit.yujin.smartcateringbackend.common.Result;
import com.nuit.yujin.smartcateringbackend.entity.Dish;
import com.nuit.yujin.smartcateringbackend.service.DishService;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/merchant/dish")
public class MerchantDishController {

    private final DishService dishService;

    public MerchantDishController(DishService dishService) {
        this.dishService = dishService;
    }

    @GetMapping("/page")
    public Result<IPage<Dish>> page(@RequestParam(defaultValue = "1") Long storeId,
                                    @RequestParam(defaultValue = "1") Integer pageNo,
                                    @RequestParam(defaultValue = "10") Integer pageSize,
                                    @RequestParam(required = false) String keyword,
                                    @RequestParam(required = false) Long categoryId,
                                    @RequestParam(required = false) String status) {
        return Result.success(dishService.pageDish(storeId, pageNo, pageSize, keyword, categoryId, status));
    }

    @PostMapping
    public Result<Void> create(@RequestParam(defaultValue = "1") Long storeId, @RequestBody Dish dish) {
        dishService.createDish(storeId, dish);
        return Result.success(null);
    }

    @PutMapping("/{id}")
    public Result<Void> update(@RequestParam(defaultValue = "1") Long storeId, @PathVariable Long id, @RequestBody Dish dish) {
        dishService.updateDish(storeId, id, dish);
        return Result.success(null);
    }

    @DeleteMapping("/{id}")
    public Result<Void> delete(@RequestParam(defaultValue = "1") Long storeId, @PathVariable Long id) {
        dishService.deleteDish(storeId, id);
        return Result.success(null);
    }

    @PutMapping("/{id}/status")
    public Result<Void> updateStatus(@RequestParam(defaultValue = "1") Long storeId,
                                     @PathVariable Long id,
                                     @RequestParam String status) {
        dishService.updateStatus(storeId, id, status);
        return Result.success(null);
    }
}
