package com.beyond.beatbuddy.global.security;

import com.beyond.beatbuddy.global.util.JwtUtil;
import io.jsonwebtoken.Claims;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.messaging.Message;
import org.springframework.messaging.MessageChannel;
import org.springframework.messaging.MessagingException;
import org.springframework.messaging.simp.stomp.StompCommand;
import org.springframework.messaging.simp.stomp.StompHeaderAccessor;
import org.springframework.messaging.support.ChannelInterceptor;
import org.springframework.stereotype.Component;

// JWT 인증/인가 처리를 수행하기 위해 Spring의 ChannelInterceptor를 구현한 StompHandler
@Slf4j
@Component
@RequiredArgsConstructor
public class StompHandler implements ChannelInterceptor {

    private final JwtUtil jwtUtil;
    private final RedisTemplate<String, String> redisTemplate;

    @Override
    public Message<?> preSend(Message<?> message, MessageChannel channel) {
        StompHeaderAccessor accessor = StompHeaderAccessor.wrap(message);

        if (StompCommand.CONNECT.equals(accessor.getCommand())) {
            String bearerToken = accessor.getFirstNativeHeader("Authorization");

            if (bearerToken != null && bearerToken.startsWith("Bearer ")) {
                String token = jwtUtil.substringToken(bearerToken);

                // 블랙리스트 검증
                if (redisTemplate.hasKey("blacklist:" + token)) {
                    throw new MessagingException("로그아웃된 토큰");
                }
                // JWT 검증
                try{
                    Claims claims = jwtUtil.extractClaims(token);

                    Long userId = Long.valueOf(claims.getSubject());
                    String email = claims.get("email", String.class);
                    String nickname = claims.get("nickname", String.class);

                    accessor.getSessionAttributes().put("userId", userId);
                    accessor.getSessionAttributes().put("email", email);
                    accessor.getSessionAttributes().put("nickname", nickname);

                    log.info("[WebSocket 인증 성공] userId: {}, email: {}", userId, email);
                } catch (Exception e){
                    log.error("WebSocket 인증 실패 {}", e.getMessage());
                    throw new MessagingException("JWT 인증 실패");
                }
            }
        }

        if (StompCommand.SEND.equals(accessor.getCommand())) {
            Object userId = accessor.getSessionAttributes().get("userId");

            if (userId == null) {
                log.warn("SEND: WebSocket 세션에 사용자 정보 없음");
                throw new MessagingException("세션 인증 정보 없음");
            }

            log.info("SEND: userId={} ", userId);
        }
        return message;
    }
}