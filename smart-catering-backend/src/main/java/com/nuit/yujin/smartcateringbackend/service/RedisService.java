package com.nuit.yujin.smartcateringbackend.service;

import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.time.Duration;

@Service
public class RedisService {

    private final RedisTemplate<String, Object> redisTemplate;

    public RedisService(RedisTemplate<String, Object> redisTemplate) {
        this.redisTemplate = redisTemplate;
    }

    public Object get(String key) {
        return redisTemplate.opsForValue().get(key);
    }

    public void set(String key, Object value, Duration ttl) {
        redisTemplate.opsForValue().set(key, value, ttl);
    }

    public void delete(String key) {
        redisTemplate.delete(key);
    }

    public String hotDishKey(Long storeId) {
        return "dish:hot:store:" + storeId;
    }

    public String userInfoKey(Long userId) {
        return "user:info:" + userId;
    }

    public String tableStatusKey(Long tableId) {
        return "table:status:" + tableId;
    }
}
