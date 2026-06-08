package com.nuit.yujin.smartcateringbackend.websocket;

import com.nuit.yujin.smartcateringbackend.entity.DiningTable;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.CloseStatus;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;
import org.springframework.web.socket.handler.TextWebSocketHandler;

import java.io.IOException;
import java.net.URI;
import java.util.Arrays;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

@Component
public class TableStatusWebSocketHandler extends TextWebSocketHandler {

    private final Map<Long, Set<WebSocketSession>> storeSessions = new ConcurrentHashMap<>();

    @Override
    public void afterConnectionEstablished(WebSocketSession session) {
        Long storeId = getStoreId(session);
        storeSessions.computeIfAbsent(storeId, key -> ConcurrentHashMap.newKeySet()).add(session);
    }

    @Override
    public void afterConnectionClosed(WebSocketSession session, CloseStatus status) {
        Long storeId = getStoreId(session);
        Set<WebSocketSession> sessions = storeSessions.get(storeId);
        if (sessions != null) {
            sessions.remove(session);
            if (sessions.isEmpty()) {
                storeSessions.remove(storeId);
            }
        }
    }

    public void pushTableStatusChanged(DiningTable table) {
        String message = """
                {"type":"TABLE_STATUS_CHANGED","storeId":%d,"tableId":%d,"tableNo":"%s","status":"%s","seats":%d}
                """.formatted(
                table.getStoreId(),
                table.getId(),
                table.getTableNo(),
                table.getStatus(),
                table.getSeats() == null ? 0 : table.getSeats()
        );
        sendToStore(table.getStoreId(), message);
    }

    private void sendToStore(Long storeId, String message) {
        Set<WebSocketSession> sessions = storeSessions.get(storeId);
        if (sessions == null || sessions.isEmpty()) {
            return;
        }
        for (WebSocketSession session : sessions) {
            if (session.isOpen()) {
                try {
                    session.sendMessage(new TextMessage(message));
                } catch (IOException e) {
                    throw new RuntimeException("桌台状态推送失败", e);
                }
            }
        }
    }

    private Long getStoreId(WebSocketSession session) {
        URI uri = session.getUri();
        String query = uri == null ? null : uri.getQuery();
        if (query == null || query.isBlank()) {
            return 1L;
        }
        return Arrays.stream(query.split("&"))
                .map(item -> item.split("=", 2))
                .filter(parts -> parts.length == 2 && "storeId".equals(parts[0]))
                .map(parts -> Long.valueOf(parts[1]))
                .findFirst()
                .orElse(1L);
    }
}
