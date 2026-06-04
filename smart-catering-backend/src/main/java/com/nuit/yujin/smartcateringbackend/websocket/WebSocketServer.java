package com.nuit.yujin.smartcateringbackend.websocket;

import com.nuit.yujin.smartcateringbackend.mq.OrderCreatedMessage;
import org.springframework.stereotype.Component;

@Component
public class WebSocketServer {

    private final MerchantOrderWebSocketHandler merchantOrderWebSocketHandler;
    private final OrderWebSocketHandler orderWebSocketHandler;

    public WebSocketServer(MerchantOrderWebSocketHandler merchantOrderWebSocketHandler,
                           OrderWebSocketHandler orderWebSocketHandler) {
        this.merchantOrderWebSocketHandler = merchantOrderWebSocketHandler;
        this.orderWebSocketHandler = orderWebSocketHandler;
    }

    public void pushNewOrderToMerchant(OrderCreatedMessage message) {
        merchantOrderWebSocketHandler.pushNewOrder(message);
    }

    public void pushOrderStatusToUser(Long orderId, String status) {
        orderWebSocketHandler.broadcastOrderStatus(orderId, status);
    }
}
