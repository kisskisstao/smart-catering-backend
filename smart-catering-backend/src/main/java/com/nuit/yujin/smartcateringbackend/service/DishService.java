package com.nuit.yujin.smartcateringbackend.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.nuit.yujin.smartcateringbackend.entity.Dish;
import com.nuit.yujin.smartcateringbackend.mapper.DishMapper;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.List;

@Service
public class DishService extends ServiceImpl<DishMapper, Dish> {

    private final RedisService redisService;

    public DishService(RedisService redisService) {
        this.redisService = redisService;
    }

    public IPage<Dish> pageDish(Long storeId, Integer pageNo, Integer pageSize, String keyword, Long categoryId, String status) {
        LambdaQueryWrapper<Dish> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Dish::getStoreId, storeId);
        wrapper.and(keyword != null && !keyword.isBlank(), w ->
                w.like(Dish::getName, keyword).or().like(Dish::getDescription, keyword)
        );
        wrapper.eq(categoryId != null, Dish::getCategoryId, categoryId);
        wrapper.eq(status != null && !status.isBlank(), Dish::getStatus, status);
        wrapper.orderByDesc(Dish::getCreateTime);
        return page(new Page<>(pageNo, pageSize), wrapper);
    }

    public List<Dish> listForUser(Long storeId, String type, String keyword, String sortType) {
        if ("HOT".equals(type) && (keyword == null || keyword.isBlank()) && (sortType == null || sortType.isBlank())) {
            Object cached = redisService.get(redisService.hotDishKey(storeId));
            if (cached instanceof List<?> list) {
                return list.stream().filter(Dish.class::isInstance).map(Dish.class::cast).toList();
            }
        }

        LambdaQueryWrapper<Dish> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Dish::getStoreId, storeId);
        wrapper.eq(Dish::getStatus, "ON");
        wrapper.and(keyword != null && !keyword.isBlank(), w ->
                w.like(Dish::getName, keyword).or().like(Dish::getDescription, keyword)
        );

        if ("RECOMMEND".equals(type)) {
            wrapper.eq(Dish::getRecommended, true).orderByDesc(Dish::getRecommendWeight);
        } else {
            wrapper.orderByDesc(Dish::getSalesCount);
        }

        if ("PRICE_ASC".equals(sortType)) {
            wrapper.orderByAsc(Dish::getPrice);
        } else if ("PRICE_DESC".equals(sortType)) {
            wrapper.orderByDesc(Dish::getPrice);
        }

        List<Dish> dishes = list(wrapper);
        if ("HOT".equals(type)) {
            redisService.set(redisService.hotDishKey(storeId), dishes, Duration.ofMinutes(10));
        }
        return dishes;
    }

    public void createDish(Long storeId, Dish dish) {
        dish.setStoreId(storeId);
        dish.setStatus(dish.getStatus() == null ? "ON" : dish.getStatus());
        dish.setStock(dish.getStock() == null ? 0 : dish.getStock());
        dish.setSalesCount(dish.getSalesCount() == null ? 0 : dish.getSalesCount());
        dish.setVersion(0);
        dish.setCreateTime(LocalDateTime.now());
        dish.setUpdateTime(LocalDateTime.now());
        save(dish);
        redisService.delete(redisService.hotDishKey(storeId));
    }

    public void updateDish(Long storeId, Long id, Dish updateData) {
        Dish existed = getById(id);
        if (existed == null || !existed.getStoreId().equals(storeId)) {
            throw new RuntimeException("菜品不存在");
        }
        updateData.setId(id);
        updateData.setStoreId(storeId);
        updateData.setUpdateTime(LocalDateTime.now());
        updateById(updateData);
        redisService.delete(redisService.hotDishKey(storeId));
    }

    public void deleteDish(Long storeId, Long id) {
        Dish existed = getById(id);
        if (existed == null || !existed.getStoreId().equals(storeId)) {
            throw new RuntimeException("菜品不存在");
        }
        removeById(id);
        redisService.delete(redisService.hotDishKey(storeId));
    }

    public void updateStatus(Long storeId, Long id, String status) {
        Dish dish = getById(id);
        if (dish == null || !dish.getStoreId().equals(storeId)) {
            throw new RuntimeException("菜品不存在");
        }
        Dish update = new Dish();
        update.setId(id);
        update.setStatus(status);
        update.setUpdateTime(LocalDateTime.now());
        updateById(update);
        redisService.delete(redisService.hotDishKey(storeId));
    }

    public Dish requireAvailableDish(Long dishId) {
        Dish dish = getById(dishId);
        if (dish == null || !"ON".equals(dish.getStatus())) {
            throw new RuntimeException("菜品不存在或已下架");
        }
        return dish;
    }

    public void deductStock(Long dishId, Integer quantity) {
        if (quantity == null || quantity <= 0) {
            throw new RuntimeException("购买数量不合法");
        }
        Dish dish = requireAvailableDish(dishId);
        if (dish.getStock() == null || dish.getStock() < quantity) {
            throw new RuntimeException(dish.getName() + "库存不足");
        }
        int updated = baseMapper.deductStockWithVersion(dishId, quantity, dish.getVersion());
        if (updated == 0) {
            throw new RuntimeException("库存更新失败，请重试");
        }
        redisService.delete(redisService.hotDishKey(dish.getStoreId()));
    }

    public void restoreStock(Long dishId, Integer quantity) {
        if (dishId == null || quantity == null || quantity <= 0) {
            return;
        }
        Dish dish = getById(dishId);
        if (dish == null) {
            return;
        }
        baseMapper.restoreStock(dishId, quantity);
        redisService.delete(redisService.hotDishKey(dish.getStoreId()));
    }
}