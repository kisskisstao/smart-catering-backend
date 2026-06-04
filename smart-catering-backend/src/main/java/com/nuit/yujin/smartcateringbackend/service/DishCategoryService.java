package com.nuit.yujin.smartcateringbackend.service;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.nuit.yujin.smartcateringbackend.entity.Dish;
import com.nuit.yujin.smartcateringbackend.entity.DishCategory;
import com.nuit.yujin.smartcateringbackend.mapper.DishCategoryMapper;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class DishCategoryService extends ServiceImpl<DishCategoryMapper, DishCategory> {

    private final DishService dishService;

    public DishCategoryService(DishService dishService) {
        this.dishService = dishService;
    }

    public List<DishCategory> listByStore(Long storeId) {
        return lambdaQuery()
                .eq(DishCategory::getStoreId, storeId)
                .orderByAsc(DishCategory::getSort)
                .list();
    }

    public void createCategory(Long storeId, DishCategory category) {
        category.setStoreId(storeId);
        category.setStatus(category.getStatus() == null ? "ENABLE" : category.getStatus());
        category.setSort(category.getSort() == null ? 0 : category.getSort());
        category.setCreateTime(LocalDateTime.now());
        category.setUpdateTime(LocalDateTime.now());
        save(category);
    }

    public void updateCategory(Long storeId, Long id, DishCategory updateData) {
        DishCategory category = getById(id);
        if (category == null || !category.getStoreId().equals(storeId)) {
            throw new RuntimeException("分类不存在");
        }
        updateData.setId(id);
        updateData.setStoreId(storeId);
        updateData.setUpdateTime(LocalDateTime.now());
        updateById(updateData);
    }

    public void deleteCategory(Long storeId, Long id) {
        long count = dishService.lambdaQuery()
                .eq(Dish::getStoreId, storeId)
                .eq(Dish::getCategoryId, id)
                .count();
        if (count > 0) {
            throw new RuntimeException("分类下存在菜品，不能删除");
        }
        removeById(id);
    }
}
