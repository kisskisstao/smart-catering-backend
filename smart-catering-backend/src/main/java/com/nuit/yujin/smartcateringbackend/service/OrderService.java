package com.nuit.yujin.smartcateringbackend.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.nuit.yujin.smartcateringbackend.dto.CreateOrderDTO;
import com.nuit.yujin.smartcateringbackend.dto.CreateOrderItemDTO;
import com.nuit.yujin.smartcateringbackend.entity.DiningOrder;
import com.nuit.yujin.smartcateringbackend.entity.DiningOrderItem;
import com.nuit.yujin.smartcateringbackend.entity.DiningTable;
import com.nuit.yujin.smartcateringbackend.entity.Dish;
import com.nuit.yujin.smartcateringbackend.enums.OrderStatus;
import com.nuit.yujin.smartcateringbackend.mapper.DiningOrderItemMapper;
import com.nuit.yujin.smartcateringbackend.mapper.DiningOrderMapper;
import com.nuit.yujin.smartcateringbackend.mq.OrderCreatedMessage;
import com.nuit.yujin.smartcateringbackend.mq.OrderProducer;
import com.nuit.yujin.smartcateringbackend.mq.OrderStatusChangedMessage;
import com.nuit.yujin.smartcateringbackend.vo.CreateOrderResultVO;
import com.nuit.yujin.smartcateringbackend.vo.OrderDetailVO;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionSynchronization;
import org.springframework.transaction.support.TransactionSynchronizationManager;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.concurrent.ThreadLocalRandom;

@Service
public class OrderService extends ServiceImpl<DiningOrderMapper, DiningOrder> {

    private static final long PAY_LIMIT_MINUTES = 15;

    private final DiningOrderItemMapper orderItemMapper;
    private final DiningTableService diningTableService;
    private final DishService dishService;
    private final OrderProducer orderProducer;

    public OrderService(DiningOrderItemMapper orderItemMapper,
                        DiningTableService diningTableService,
                        DishService dishService,
                        OrderProducer orderProducer) {
        this.orderItemMapper = orderItemMapper;
        this.diningTableService = diningTableService;
        this.dishService = dishService;
        this.orderProducer = orderProducer;
    }

    @Transactional
    public CreateOrderResultVO createOrder(Long userId, CreateOrderDTO dto) {
        if (dto.getTableId() == null) {
            throw new RuntimeException("桌台不能为空");
        }
        if (dto.getItems() == null || dto.getItems().isEmpty()) {
            throw new RuntimeException("购物车为空");
        }

        DiningTable table = diningTableService.requireUsableTable(dto.getTableId());
        DiningOrder order = new DiningOrder();
        order.setOrderNo(generateOrderNo());
        order.setUserId(userId);
        order.setStoreId(table.getStoreId());
        order.setTableId(table.getId());
        order.setStatus(OrderStatus.PENDING_PAYMENT.name());
        order.setPayStatus("UNPAID");
        order.setTotalAmount(BigDecimal.ZERO);
        order.setCreateTime(LocalDateTime.now());
        order.setUpdateTime(LocalDateTime.now());
        save(order);

        BigDecimal totalAmount = BigDecimal.ZERO;
        for (CreateOrderItemDTO item : dto.getItems()) {
            Dish dish = dishService.requireAvailableDish(item.getDishId());
            dishService.deductStock(item.getDishId(), item.getQuantity());

            BigDecimal amount = dish.getPrice().multiply(BigDecimal.valueOf(item.getQuantity()));
            totalAmount = totalAmount.add(amount);

            DiningOrderItem orderItem = new DiningOrderItem();
            orderItem.setOrderId(order.getId());
            orderItem.setDishId(dish.getId());
            orderItem.setDishName(dish.getName());
            orderItem.setPrice(dish.getPrice());
            orderItem.setQuantity(item.getQuantity());
            orderItem.setSpicy(item.getSpicy());
            orderItem.setSize(item.getSize());
            orderItem.setAmount(amount);
            orderItemMapper.insert(orderItem);
        }

        order.setTotalAmount(totalAmount);
        order.setUpdateTime(LocalDateTime.now());
        updateById(order);

        return new CreateOrderResultVO(order.getId(), order.getOrderNo(), totalAmount, order.getStatus(), order.getPayStatus());
    }

    @Transactional
    public void mockPay(Long userId, Long orderId) {
        DiningOrder order = requireUserOrder(userId, orderId);
        if (OrderStatus.CANCELLED.name().equals(order.getStatus())) {
            throw new RuntimeException("订单已取消，不能继续支付");
        }
        if (!OrderStatus.PENDING_PAYMENT.name().equals(order.getStatus())) {
            throw new RuntimeException("当前订单不需要支付");
        }
        if (order.getCreateTime().plusMinutes(PAY_LIMIT_MINUTES).isBefore(LocalDateTime.now())) {
            cancelPendingOrder(order);
            throw new RuntimeException("支付已超时，订单已取消");
        }

        order.setPayStatus("PAID");
        order.setStatus(OrderStatus.WAIT_ACCEPT.name());
        order.setUpdateTime(LocalDateTime.now());
        updateById(order);

        OrderCreatedMessage createdMessage = new OrderCreatedMessage(
                order.getId(), order.getOrderNo(), order.getStoreId(), order.getUserId(), order.getTotalAmount()
        );
        OrderStatusChangedMessage statusMessage = new OrderStatusChangedMessage(
                order.getId(), order.getStoreId(), order.getUserId(), order.getStatus()
        );
        TransactionSynchronizationManager.registerSynchronization(new TransactionSynchronization() {
            @Override
            public void afterCommit() {
                orderProducer.sendOrderCreated(createdMessage);
                orderProducer.sendOrderStatusChanged(statusMessage);
            }
        });
    }

    @Transactional
    public void cancelUserOrder(Long userId, Long orderId) {
        DiningOrder order = requireUserOrder(userId, orderId);
        if (!OrderStatus.PENDING_PAYMENT.name().equals(order.getStatus())) {
            throw new RuntimeException("只有待支付订单可以取消");
        }
        cancelPendingOrder(order);
    }

    @Transactional
    public void updateStatus(Long storeId, Long orderId, String nextStatus) {
        DiningOrder order = getById(orderId);
        if (order == null || !order.getStoreId().equals(storeId)) {
            throw new RuntimeException("订单不存在");
        }
        OrderStatus.validateTransfer(order.getStatus(), nextStatus);
        order.setStatus(nextStatus);
        order.setUpdateTime(LocalDateTime.now());
        updateById(order);

        OrderStatusChangedMessage message = new OrderStatusChangedMessage(
                order.getId(), order.getStoreId(), order.getUserId(), nextStatus
        );
        TransactionSynchronizationManager.registerSynchronization(new TransactionSynchronization() {
            @Override
            public void afterCommit() {
                orderProducer.sendOrderStatusChanged(message);
            }
        });
    }

    public IPage<DiningOrder> pageUserHistory(Long userId, String status, Integer pageNo, Integer pageSize) {
        LambdaQueryWrapper<DiningOrder> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(DiningOrder::getUserId, userId);
        wrapper.eq(status != null && !"ALL".equals(status), DiningOrder::getStatus, status);
        wrapper.orderByDesc(DiningOrder::getCreateTime);
        return page(new Page<>(pageNo, pageSize), wrapper);
    }

    public IPage<DiningOrder> pageMerchantOrders(Long storeId, String status, Integer pageNo, Integer pageSize) {
        LambdaQueryWrapper<DiningOrder> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(DiningOrder::getStoreId, storeId);
        wrapper.eq(status != null && !"ALL".equals(status), DiningOrder::getStatus, status);
        wrapper.orderByDesc(DiningOrder::getCreateTime);
        return page(new Page<>(pageNo, pageSize), wrapper);
    }

    public OrderDetailVO getUserOrderDetail(Long userId, Long orderId) {
        DiningOrder order = getById(orderId);
        if (order == null || !order.getUserId().equals(userId)) {
            throw new RuntimeException("订单不存在或无权查看");
        }
        return buildDetail(order);
    }

    public OrderDetailVO getMerchantOrderDetail(Long storeId, Long orderId) {
        DiningOrder order = getById(orderId);
        if (order == null || !order.getStoreId().equals(storeId)) {
            throw new RuntimeException("订单不存在或无权查看");
        }
        return buildDetail(order);
    }

    private DiningOrder requireUserOrder(Long userId, Long orderId) {
        DiningOrder order = getById(orderId);
        if (order == null || !order.getUserId().equals(userId)) {
            throw new RuntimeException("订单不存在或无权操作");
        }
        return order;
    }

    private void cancelPendingOrder(DiningOrder order) {
        List<DiningOrderItem> items = orderItemMapper.selectList(
                new LambdaQueryWrapper<DiningOrderItem>().eq(DiningOrderItem::getOrderId, order.getId())
        );
        for (DiningOrderItem item : items) {
            dishService.restoreStock(item.getDishId(), item.getQuantity());
        }
        order.setPayStatus("CANCELLED");
        order.setStatus(OrderStatus.CANCELLED.name());
        order.setUpdateTime(LocalDateTime.now());
        updateById(order);

        OrderStatusChangedMessage message = new OrderStatusChangedMessage(
                order.getId(), order.getStoreId(), order.getUserId(), order.getStatus()
        );
        TransactionSynchronizationManager.registerSynchronization(new TransactionSynchronization() {
            @Override
            public void afterCommit() {
                orderProducer.sendOrderStatusChanged(message);
            }
        });
    }

    private OrderDetailVO buildDetail(DiningOrder order) {
        DiningTable table = diningTableService.getById(order.getTableId());
        List<DiningOrderItem> items = orderItemMapper.selectList(
                new LambdaQueryWrapper<DiningOrderItem>().eq(DiningOrderItem::getOrderId, order.getId())
        );

        OrderDetailVO vo = new OrderDetailVO();
        vo.setId(order.getId());
        vo.setOrderNo(order.getOrderNo());
        vo.setTableId(order.getTableId());
        vo.setTableNo(table == null ? null : table.getTableNo());
        vo.setStatus(order.getStatus());
        vo.setStatusText(OrderStatus.textOf(order.getStatus()));
        vo.setPayStatus(order.getPayStatus());
        vo.setTotalAmount(order.getTotalAmount());
        vo.setCreateTime(order.getCreateTime());
        vo.setItems(items);
        return vo;
    }

    private String generateOrderNo() {
        String time = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMddHHmmssSSS"));
        int random = ThreadLocalRandom.current().nextInt(1000, 9999);
        return "OD" + time + random;
    }
}