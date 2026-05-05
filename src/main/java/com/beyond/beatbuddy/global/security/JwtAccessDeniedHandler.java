package com.beyond.beatbuddy.global.security;

import com.beyond.beatbuddy.global.dto.ApiResponse;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.web.access.AccessDeniedHandler;
import org.springframework.stereotype.Component;

import java.io.IOException;

// Security가 자동으로 ex) 일반 유저가 /api/admin/** 접근 → Security가 자동으로 403
// Role/URL 기반 접근 제한, 필터에서 터진 예외를 잡는거임
// 토큰은 있는데 권한 없음 - 403 던져
@Component
@RequiredArgsConstructor
public class JwtAccessDeniedHandler implements AccessDeniedHandler {

    private final ObjectMapper objectMapper;

    @Override
    public void handle(HttpServletRequest request,
                       HttpServletResponse response,
                       AccessDeniedException accessDeniedException) throws IOException {

        response.setStatus(HttpServletResponse.SC_FORBIDDEN);
        response.setContentType("application/json;charset=UTF-8");

        ApiResponse<?> apiResponse = ApiResponse.builder()
                .status(403)
                .message("접근 권한이 없습니다.")
                .result(null)
                .build();

        response.getWriter().write(objectMapper.writeValueAsString(apiResponse));
    }
}