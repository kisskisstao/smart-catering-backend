package com.nuit.yujin.smartcateringbackend.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.nuit.yujin.smartcateringbackend.common.Result;
import com.nuit.yujin.smartcateringbackend.entity.DiningOrder;
import com.nuit.yujin.smartcateringbackend.entity.Dish;
import com.nuit.yujin.smartcateringbackend.entity.DishCategory;
import com.nuit.yujin.smartcateringbackend.service.DishCategoryService;
import com.nuit.yujin.smartcateringbackend.service.DishService;
import com.nuit.yujin.smartcateringbackend.service.OrderService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

@RestController
public class MerchantDashboardController {

    private final OrderService orderService;
    private final DishService dishService;
    private final DishCategoryService categoryService;

    public MerchantDashboardController(OrderService orderService,
                                       DishService dishService,
                                       DishCategoryService categoryService) {
        this.orderService = orderService;
        this.dishService = dishService;
        this.categoryService = categoryService;
    }

    @GetMapping("/merchant/dashboard")
    public Result<Map<String, Object>> dashboard(@RequestParam(defaultValue = "1") Long storeId) {
        LocalDate today = LocalDate.now();
        LocalDateTime todayStart = today.atStartOfDay();
        LocalDateTime tomorrowStart = today.plusDays(1).atStartOfDay();
        LocalDateTime sevenDaysStart = today.minusDays(6).atStartOfDay();

        List<DiningOrder> todayOrders = orderService.list(new LambdaQueryWrapper<DiningOrder>()
                .eq(DiningOrder::getStoreId, storeId)
                .ge(DiningOrder::getCreateTime, todayStart)
                .lt(DiningOrder::getCreateTime, tomorrowStart));

        long todayOrderCount = todayOrders.size();
        long waitAcceptCount = todayOrders.stream()
                .filter(order -> "WAIT_ACCEPT".equals(order.getStatus()))
                .count();
        BigDecimal todayRevenue = todayOrders.stream()
                .filter(order -> "PAID".equals(order.getPayStatus()))
                .map(DiningOrder::getTotalAmount)
                .filter(Objects::nonNull)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        List<DiningOrder> sevenDayOrders = orderService.list(new LambdaQueryWrapper<DiningOrder>()
                .eq(DiningOrder::getStoreId, storeId)
                .ge(DiningOrder::getCreateTime, sevenDaysStart));
        Map<LocalDate, Long> trendMap = sevenDayOrders.stream()
                .collect(Collectors.groupingBy(order -> order.getCreateTime().toLocalDate(), Collectors.counting()));
        List<Map<String, Object>> trend = new ArrayList<>();
        for (int i = 6; i >= 0; i--) {
            LocalDate date = today.minusDays(i);
            Map<String, Object> item = new LinkedHashMap<>();
            item.put("date", date.toString());
            item.put("count", trendMap.getOrDefault(date, 0L));
            trend.add(item);
        }

        List<Dish> dishes = dishService.list(new LambdaQueryWrapper<Dish>().eq(Dish::getStoreId, storeId));
        List<Map<String, Object>> dishRank = dishes.stream()
                .sorted(Comparator.comparingInt(dish -> -(dish.getSalesCount() == null ? 0 : dish.getSalesCount())))
                .limit(10)
                .map(dish -> {
                    Map<String, Object> item = new LinkedHashMap<>();
                    item.put("name", dish.getName());
                    item.put("sales", dish.getSalesCount() == null ? 0 : dish.getSalesCount());
                    return item;
                })
                .collect(Collectors.toList());

        Map<Long, DishCategory> categoryMap = categoryService.list(new LambdaQueryWrapper<DishCategory>()
                        .eq(DishCategory::getStoreId, storeId))
                .stream()
                .collect(Collectors.toMap(DishCategory::getId, Function.identity(), (a, b) -> a));
        Map<String, Long> categoryCountMap = dishes.stream()
                .collect(Collectors.groupingBy(dish -> {
                    DishCategory category = categoryMap.get(dish.getCategoryId());
                    return category == null ? "未分类" : category.getName();
                }, Collectors.counting()));
        List<Map<String, Object>> categoryRatio = categoryCountMap.entrySet().stream()
                .map(entry -> {
                    Map<String, Object> item = new LinkedHashMap<>();
                    item.put("name", entry.getKey());
                    item.put("value", entry.getValue());
                    return item;
                })
                .collect(Collectors.toList());

        Map<String, Object> summary = new HashMap<>();
        summary.put("todayOrders", todayOrderCount);
        summary.put("todayRevenue", todayRevenue);
        summary.put("waitAcceptCount", waitAcceptCount);

        Map<String, Object> result = new HashMap<>();
        result.put("summary", summary);
        result.put("trend", trend);
        result.put("dishRank", dishRank);
        result.put("categoryRatio", categoryRatio);
        return Result.success(result);
    }
}
