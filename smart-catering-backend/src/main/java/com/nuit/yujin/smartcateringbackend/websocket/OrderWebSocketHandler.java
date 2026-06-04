package com.nuit.yujin.smartcateringbackend.websocket;

import org.springframework.stereotype.Component;
import org.springframework.web.socket.CloseStatus;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;
import org.springframework.web.socket.handler.TextWebSocketHandler;

import java.io.IOException;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

@Component
public class OrderWebSocketHandler extends TextWebSocketHandler {

    private final Map<Long, Set<WebSocketSession>> orderSessions = new ConcurrentHashMap<>();

    @Override
    public void afterConnectionEstablished(WebSocketSession session) {
        Long orderId = getOrderId(session);
        orderSessions.computeIfAbsent(orderId, key -> ConcurrentHashMap.newKeySet()).add(session);
    }

    @Override
    public void afterConnectionClosed(WebSocketSession session, CloseStatus status) {
        Long orderId = getOrderId(session);
        Set<WebSocketSession> sessions = orderSessions.get(orderId);
        if (sessions != null) {
            sessions.remove(session);
            if (sessions.isEmpty()) {
                orderSessions.remove(orderId);
            }
        }
    }

    public void broadcastOrderStatus(Long orderId, String status) {
        String message = """
                {"type":"ORDER_STATUS_CHANGE","orderId":%d,"status":"%s"}
                """.formatted(orderId, status);
        sendToOrder(orderId, message);
    }

    private void sendToOrder(Long orderId, String message) {
        Set<WebSocketSession> sessions = orderSessions.get(orderId);
        if (sessions == null || sessions.isEmpty()) {
            return;
        }
        for (WebSocketSession session : sessions) {
            if (session.isOpen()) {
                try {
                    session.sendMessage(new TextMessage(message));
                } catch (IOException e) {
                    throw new RuntimeException("订单状态推送失败", e);
                }
            }
        }
    }

    private Long getOrderId(WebSocketSession session) {
        String path = session.getUri().getPath();
        return Long.valueOf(path.substring(path.lastIndexOf("/") + 1));
    }
}
