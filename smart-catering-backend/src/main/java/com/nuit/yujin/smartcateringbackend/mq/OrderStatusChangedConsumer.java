package com.nuit.yujin.smartcateringbackend.mq;

import com.nuit.yujin.smartcateringbackend.config.RabbitMQConfig;
import com.nuit.yujin.smartcateringbackend.websocket.WebSocketServer;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

@Component
public class OrderStatusChangedConsumer {

    private final WebSocketServer webSocketServer;

    public OrderStatusChangedConsumer(WebSocketServer webSocketServer) {
        this.webSocketServer = webSocketServer;
    }

    @RabbitListener(queues = RabbitMQConfig.ORDER_STATUS_CHANGED_QUEUE)
    public void handle(OrderStatusChangedMessage message) {
        webSocketServer.pushOrderStatusToUser(message.getOrderId(), message.getStatus());
    }
}
