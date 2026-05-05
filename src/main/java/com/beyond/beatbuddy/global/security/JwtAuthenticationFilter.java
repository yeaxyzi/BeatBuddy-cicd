package com.beyond.beatbuddy.global.security;

import com.beyond.beatbuddy.global.util.JwtUtil;
import com.beyond.beatbuddy.global.util.RedisService;
import io.jsonwebtoken.JwtException;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.List;

@Slf4j
@RequiredArgsConstructor
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private final JwtUtil jwtUtil;
    private final RedisService redisService;

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain filterChain)
            throws ServletException, IOException {

        log.info("Filter 실행: {}", request.getRequestURI());

        if (request.getRequestURI().equals("/api/v1/auth/token/refresh")) {
            filterChain.doFilter(request, response);
            return;
        }

        String bearerToken = request.getHeader("Authorization");
        log.info("bearerToken: {}", bearerToken);

        // 토큰 없으면 그냥 통과 (공개 API는 SecurityConfig에서 permitAll)
        if (bearerToken == null || !bearerToken.startsWith("Bearer ")) {
            log.info("토큰 없음, 통과");  // ← 추가
            filterChain.doFilter(request, response);
            return;
        }

        try {
            // 1. Bearer 제거
            String token = jwtUtil.substringToken(bearerToken);

            // 2. 블랙리스트 확인
            if (redisService.isBlacklisted(token)) {
                log.warn("블랙리스트 토큰 요청: {}", request.getRequestURI());
                sendErrorResponse(response, "로그아웃된 토큰입니다.");
                return;
            }

            // 3. 토큰 유효성 검증
            jwtUtil.validateToken(token);

            // 4. email이랑 userId 파싱해서 SecurityContext에 등록
            Long userId = jwtUtil.getUserId(token);
            String email = jwtUtil.getEmail(token);

            UserPrincipal userPrincipal = new UserPrincipal(userId, email);

            UsernamePasswordAuthenticationToken authentication =
                    new UsernamePasswordAuthenticationToken(userPrincipal, null, List.of());
            SecurityContextHolder.getContext().setAuthentication(authentication);

        } catch (IllegalArgumentException | JwtException e) {
            log.warn("유효하지 않은 토큰: {}", e.getMessage());
            sendErrorResponse(response, "유효하지 않은 토큰입니다.");
            return;
        }

        filterChain.doFilter(request, response);
    }

    private void sendErrorResponse(HttpServletResponse response, String message) throws IOException {
        response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
        response.setContentType("application/json;charset=UTF-8");
        response.getWriter().write(
                "{\"status\":401,\"message\":\"" + message + "\",\"result\":null}"
        );
    }
}
