package com.nuit.yujin.smartcateringbackend.config;

import com.nuit.yujin.smartcateringbackend.websocket.MerchantOrderWebSocketHandler;
import com.nuit.yujin.smartcateringbackend.websocket.OrderWebSocketHandler;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.socket.config.annotation.EnableWebSocket;
import org.springframework.web.socket.config.annotation.WebSocketConfigurer;
import org.springframework.web.socket.config.annotation.WebSocketHandlerRegistry;

@Configuration
@EnableWebSocket
public class WebSocketConfig implements WebSocketConfigurer {

    private final OrderWebSocketHandler orderWebSocketHandler;
    private final MerchantOrderWebSocketHandler merchantOrderWebSocketHandler;

    public WebSocketConfig(OrderWebSocketHandler orderWebSocketHandler,
                           MerchantOrderWebSocketHandler merchantOrderWebSocketHandler) {
        this.orderWebSocketHandler = orderWebSocketHandler;
        this.merchantOrderWebSocketHandler = merchantOrderWebSocketHandler;
    }

    @Override
    public void registerWebSocketHandlers(WebSocketHandlerRegistry registry) {
        registry.addHandler(orderWebSocketHandler, "/ws/order/{orderId}")
                .setAllowedOrigins("*");
        registry.addHandler(merchantOrderWebSocketHandler, "/ws/merchant/order")
                .setAllowedOrigins("*");
    }
}
