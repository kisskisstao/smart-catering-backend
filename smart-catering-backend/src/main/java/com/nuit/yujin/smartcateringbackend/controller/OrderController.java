package com.nuit.yujin.smartcateringbackend.controller;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.nuit.yujin.smartcateringbackend.common.Result;
import com.nuit.yujin.smartcateringbackend.dto.CreateOrderDTO;
import com.nuit.yujin.smartcateringbackend.entity.DiningOrder;
import com.nuit.yujin.smartcateringbackend.service.OrderService;
import com.nuit.yujin.smartcateringbackend.utils.JwtUtils;
import com.nuit.yujin.smartcateringbackend.vo.CreateOrderResultVO;
import com.nuit.yujin.smartcateringbackend.vo.OrderDetailVO;
import org.springframework.web.bind.annotation.*;

@RestController
public class OrderController {

    private final OrderService orderService;
    private final JwtUtils jwtUtils;

    public OrderController(OrderService orderService, JwtUtils jwtUtils) {
        this.orderService = orderService;
        this.jwtUtils = jwtUtils;
    }

    @PostMapping("/order/create")
    public Result<CreateOrderResultVO> create(@RequestHeader("Authorization") String token,
                                              @RequestBody CreateOrderDTO dto) {
        return Result.success(orderService.createOrder(jwtUtils.getUserId(token), dto));
    }

    @PostMapping("/order/{orderId}/pay")
    public Result<Void> pay(@RequestHeader("Authorization") String token,
                            @PathVariable Long orderId) {
        orderService.mockPay(jwtUtils.getUserId(token), orderId);
        return Result.success(null);
    }

    @PutMapping("/order/{orderId}/cancel")
    public Result<Void> cancel(@RequestHeader("Authorization") String token,
                               @PathVariable Long orderId) {
        orderService.cancelUserOrder(jwtUtils.getUserId(token), orderId);
        return Result.success(null);
    }

    @GetMapping("/order/history")
    public Result<IPage<DiningOrder>> history(@RequestHeader("Authorization") String token,
                                              @RequestParam(defaultValue = "ALL") String status,
                                              @RequestParam(defaultValue = "1") Integer pageNo,
                                              @RequestParam(defaultValue = "10") Integer pageSize) {
        return Result.success(orderService.pageUserHistory(jwtUtils.getUserId(token), status, pageNo, pageSize));
    }

    @GetMapping("/order/detail/{orderId}")
    public Result<OrderDetailVO> userDetail(@RequestHeader("Authorization") String token,
                                            @PathVariable Long orderId) {
        return Result.success(orderService.getUserOrderDetail(jwtUtils.getUserId(token), orderId));
    }

    @GetMapping("/merchant/order/page")
    public Result<IPage<DiningOrder>> merchantPage(@RequestParam(defaultValue = "1") Long storeId,
                                                   @RequestParam(defaultValue = "ALL") String status,
                                                   @RequestParam(defaultValue = "1") Integer pageNo,
                                                   @RequestParam(defaultValue = "10") Integer pageSize) {
        return Result.success(orderService.pageMerchantOrders(storeId, status, pageNo, pageSize));
    }

    @GetMapping("/merchant/order/detail/{orderId}")
    public Result<OrderDetailVO> merchantDetail(@RequestParam(defaultValue = "1") Long storeId,
                                                @PathVariable Long orderId) {
        return Result.success(orderService.getMerchantOrderDetail(storeId, orderId));
    }

    @PutMapping("/merchant/order/{orderId}/status")
    public Result<Void> updateStatus(@RequestParam(defaultValue = "1") Long storeId,
                                     @PathVariable Long orderId,
                                     @RequestParam String status) {
        orderService.updateStatus(storeId, orderId, status);
        return Result.success(null);
    }
}