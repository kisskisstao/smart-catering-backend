package com.nuit.yujin.smartcateringbackend.controller;

import com.nuit.yujin.smartcateringbackend.common.Result;
import com.nuit.yujin.smartcateringbackend.entity.DishCategory;
import com.nuit.yujin.smartcateringbackend.service.DishCategoryService;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/merchant/category")
public class CategoryController {

    private final DishCategoryService categoryService;

    public CategoryController(DishCategoryService categoryService) {
        this.categoryService = categoryService;
    }

    @GetMapping("/list")
    public Result<List<DishCategory>> list(@RequestParam(defaultValue = "1") Long storeId) {
        return Result.success(categoryService.listByStore(storeId));
    }

    @PostMapping
    public Result<Void> create(@RequestParam(defaultValue = "1") Long storeId, @RequestBody DishCategory category) {
        categoryService.createCategory(storeId, category);
        return Result.success(null);
    }

    @PutMapping("/{id}")
    public Result<Void> update(@RequestParam(defaultValue = "1") Long storeId,
                               @PathVariable Long id,
                               @RequestBody DishCategory category) {
        categoryService.updateCategory(storeId, id, category);
        return Result.success(null);
    }

    @DeleteMapping("/{id}")
    public Result<Void> delete(@RequestParam(defaultValue = "1") Long storeId, @PathVariable Long id) {
        categoryService.deleteCategory(storeId, id);
        return Result.success(null);
    }
}
