package com.beyond.beatbuddy.global.util;

import io.jsonwebtoken.*;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.security.Key;
import java.util.Date;

@Component
public class JwtUtil {
    private final Key key;
    private static final long accessExpiration = 1000L * 60L * 15L;             // 15분
    private static final long refreshExpiration = 1000L * 60L * 60L * 24L * 7L; // 7일

    // 생성자에서 key 만들기
    public JwtUtil(@Value("${jwt.secret}") String secret) {
        this.key = Keys.hmacShaKeyFor(secret.getBytes());
    }

    // Access Token 생성
    public String generateAccessToken(Long userId, String email, String nickname) {
        return Jwts.builder()
                .setSubject(String.valueOf(userId))
                .claim("email", email)
                .claim("nickname", nickname)
                .setIssuedAt(new Date())
                .setExpiration(new Date(System.currentTimeMillis() + accessExpiration))
                .signWith(key, SignatureAlgorithm.HS256)
                .compact();
    }

    // Refresh Token 생성
    public String generateRefreshToken(Long userId) {
        return Jwts.builder()
                .setSubject(String.valueOf(userId))
                .setIssuedAt(new Date())
                .setExpiration(new Date(System.currentTimeMillis() + refreshExpiration))
                .signWith(key, SignatureAlgorithm.HS256)
                .compact();
    }

    // "Bearer " 제거
    public String substringToken(String bearerToken) {
        if (bearerToken != null && bearerToken.startsWith("Bearer ")) {
            return bearerToken.substring(7);
        }
        throw new IllegalArgumentException("토큰 형식이 올바르지 않습니다.");
    }

    // 토큰 검증
    public void validateToken(String token) {
        try {
            extractClaims(token);
        } catch (ExpiredJwtException e) {
            throw new IllegalArgumentException("만료된 토큰입니다.");
        } catch (JwtException e) {
            throw new IllegalArgumentException("유효하지 않은 토큰입니다.");
        }
    }

    // Claims 추출
    public Claims extractClaims(String token) {
        return Jwts.parserBuilder()
                .setSigningKey(key)
                .build()
                .parseClaimsJws(token)
                .getBody();
    }

    // userId 추출
    public Long getUserId(String token) {
        return Long.valueOf(extractClaims(token).getSubject());
    }

    // email 추출
    public String getEmail(String token) {
        return extractClaims(token).get("email", String.class);
    }

    // nickname 추출
    public String getNickname(String token) {
        return extractClaims(token).get("nickname", String.class);
    }

    // 만료시간 추출 (블랙리스트 TTL용)
    public long getExpiration(String token) {
        Date expiration = extractClaims(token).getExpiration();
        return expiration.getTime() - System.currentTimeMillis();
    }
}
