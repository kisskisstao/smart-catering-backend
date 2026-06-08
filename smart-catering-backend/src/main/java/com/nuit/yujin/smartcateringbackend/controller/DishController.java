package com.nuit.yujin.smartcateringbackend.controller;

import com.nuit.yujin.smartcateringbackend.common.Result;
import com.nuit.yujin.smartcateringbackend.entity.Dish;
import com.nuit.yujin.smartcateringbackend.service.DishService;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/dish")
public class DishController {

    private final DishService dishService;

    public DishController(DishService dishService) {
        this.dishService = dishService;
    }

    @GetMapping("/list")
    public Result<List<Dish>> list(@RequestParam(defaultValue = "1") Long storeId,
                                   @RequestParam(required = false) String type,
                                   @RequestParam(required = false) String keyword,
                                   @RequestParam(required = false) String sortType) {
        return Result.success(dishService.listForUser(storeId, type, keyword, sortType));
    }

    @GetMapping("/detail/{id}")
    public Result<Dish> detail(@PathVariable Long id) {
        return Result.success(dishService.detail(id));
    }
}
