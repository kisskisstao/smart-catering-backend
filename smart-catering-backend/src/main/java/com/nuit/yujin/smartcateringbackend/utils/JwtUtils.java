package com.nuit.yujin.smartcateringbackend.utils;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

@Component
public class JwtUtils {

    private static final String SECRET_KEY = "SmartCateringSecretKey2024SmartCateringSecretKey2024";
    private static final long EXPIRE_TIME = 7 * 24 * 60 * 60 * 1000;

    private SecretKey getSigningKey() {
        return Keys.hmacShaKeyFor(SECRET_KEY.getBytes(StandardCharsets.UTF_8));
    }

    public String generateToken(Long userId, String nickname) {
        Map<String, Object> claims = new HashMap<>();
        claims.put("userId", userId);
        claims.put("nickname", nickname);
        claims.put("role", "USER");
        return buildToken(claims);
    }

    public String generateMerchantToken(Long merchantId, Long storeId, String username, String role) {
        Map<String, Object> claims = new HashMap<>();
        claims.put("merchantId", merchantId);
        claims.put("storeId", storeId);
        claims.put("username", username);
        claims.put("role", role);
        return buildToken(claims);
    }

    private String buildToken(Map<String, Object> claims) {
        return Jwts.builder()
                .claims(claims)
                .issuedAt(new Date())
                .expiration(new Date(System.currentTimeMillis() + EXPIRE_TIME))
                .signWith(getSigningKey())
                .compact();
    }

    public Claims parseToken(String token) {
        if (token != null && token.startsWith("Bearer ")) {
            token = token.substring(7);
        }
        return Jwts.parser()
                .verifyWith(getSigningKey())
                .build()
                .parseSignedClaims(token)
                .getPayload();
    }

    public Long getUserId(String token) {
        return parseToken(token).get("userId", Long.class);
    }

    public Long getStoreId(String token) {
        return parseToken(token).get("storeId", Long.class);
    }

    public String getRole(String token) {
        return parseToken(token).get("role", String.class);
    }
}
