package com.nuit.yujin.smartcateringbackend.mq;

import com.nuit.yujin.smartcateringbackend.config.RabbitMQConfig;
import com.nuit.yujin.smartcateringbackend.websocket.WebSocketServer;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

@Component
public class OrderCreatedConsumer {

    private final WebSocketServer webSocketServer;

    public OrderCreatedConsumer(WebSocketServer webSocketServer) {
        this.webSocketServer = webSocketServer;
    }

    @RabbitListener(queues = RabbitMQConfig.ORDER_CREATED_QUEUE)
    public void handle(OrderCreatedMessage message) {
        webSocketServer.pushNewOrderToMerchant(message);
    }
}
