package com.nuit.yujin.smartcateringbackend.enums;

import java.util.List;
import java.util.Map;

public enum OrderStatus {
    PENDING_PAYMENT("待支付"),
    WAIT_ACCEPT("待接单"),
    COOKING("制作中"),
    COMPLETED("已完成"),
    CANCELLED("已取消");

    private static final Map<String, List<String>> ALLOWED = Map.of(
            PENDING_PAYMENT.name(), List.of(WAIT_ACCEPT.name(), CANCELLED.name()),
            WAIT_ACCEPT.name(), List.of(COOKING.name(), CANCELLED.name()),
            COOKING.name(), List.of(COMPLETED.name()),
            COMPLETED.name(), List.of(),
            CANCELLED.name(), List.of()
    );

    private final String text;

    OrderStatus(String text) {
        this.text = text;
    }

    public String getText() {
        return text;
    }

    public static void validateTransfer(String current, String next) {
        if (!ALLOWED.getOrDefault(current, List.of()).contains(next)) {
            throw new RuntimeException("非法订单状态流转：" + current + " -> " + next);
        }
    }

    public static String textOf(String status) {
        for (OrderStatus value : values()) {
            if (value.name().equals(status)) {
                return value.getText();
            }
        }
        return "未知状态";
    }
}