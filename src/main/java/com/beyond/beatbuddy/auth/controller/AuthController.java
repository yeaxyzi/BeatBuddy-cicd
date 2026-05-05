package com.beyond.beatbuddy.auth.controller;
import com.beyond.beatbuddy.auth.dto.request.*;
import com.beyond.beatbuddy.auth.dto.response.EmailSendResponse;
import com.beyond.beatbuddy.auth.dto.response.EmailVerifyResponse;
import com.beyond.beatbuddy.auth.dto.response.TokenResponse;
import com.beyond.beatbuddy.auth.service.AuthService;
import com.beyond.beatbuddy.global.dto.ApiResponse;

import com.beyond.beatbuddy.global.exception.UnauthorizedException;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Encoding;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.*;
import org.springframework.stereotype.Controller;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.time.Duration;
import java.util.Map;

@Slf4j
@RestController
@RequestMapping("/api/v1/auth")
@RequiredArgsConstructor
@Validated
@Tag(name = "Auth APIs", description = "인증 관련 API 목록")
public class AuthController {
    private final AuthService authService;


    // 회원가입
    @PostMapping(value = "/signup", consumes = "multipart/form-data")
    @Operation(summary = "회원가입", description = "회원정보 및 사진을 multipart/form-data 형태로 보낸다.")
    public ResponseEntity<?> signup(
            @RequestPart("data") @Valid SignupRequest request,
            @RequestPart(value = "profileImage", required = false) MultipartFile profileImage,
            HttpServletResponse response) {

        TokenResponse tokenResponse = authService.signUp(request, profileImage);

        ResponseCookie cookie = createRefreshTokenCookie(tokenResponse.getRefreshToken());

        response.addHeader(HttpHeaders.SET_COOKIE, cookie.toString());

        return ApiResponse.of(HttpStatus.CREATED, "회원가입 성공",
                TokenResponse.builder()
                        .accessToken(tokenResponse.getAccessToken())
                        .userId(tokenResponse.getUserId())
                        .email(tokenResponse.getEmail())
                        .nickname(tokenResponse.getNickname())
                        .build()
        );
    }

    @PostMapping("/email/send")
    public ResponseEntity<?> sendVerificationCode(
            @RequestBody @Valid EmailSendRequest request) {
        EmailSendResponse emailSendResponse = authService.sendVerificationCode(request.getEmail());
        return ApiResponse.of(HttpStatus.OK, "인증코드를 발송했습니다.", emailSendResponse);
    }


    @PostMapping("/email/verify")
    public ResponseEntity<?> verifyCode(
            @RequestBody @Valid EmailVerifyRequest request) {
        EmailVerifyResponse response = authService.verifyCode(request.getEmail(), request.getCode());
        return ApiResponse.of(HttpStatus.OK, "인증이 완료됐습니다.", response);
    }

    @PostMapping("/login")
    @Operation(summary = "로그인", description = "아이디와 패스워드를 JSON 문자열로 받아서 로그인해용")
    public ResponseEntity<?> login(
            @RequestBody @Valid LoginRequest request,
            HttpServletResponse response) {

        TokenResponse tokenResponse = authService.login(request);

        // Refresh Token → HttpOnly Cookie
        ResponseCookie cookie = createRefreshTokenCookie(tokenResponse.getRefreshToken());
        response.addHeader(HttpHeaders.SET_COOKIE, cookie.toString());

        return ApiResponse.of(HttpStatus.OK, "로그인 성공",
                TokenResponse.builder()
                        .accessToken(tokenResponse.getAccessToken())
                        .userId(tokenResponse.getUserId())
                        .email(tokenResponse.getEmail())
                        .nickname(tokenResponse.getNickname())
                        .build()
        );
    }

    // 로그아웃
    @PostMapping("/logout")
    @Operation(summary = "로그아웃", description = "refresh token 쿠키 삭제 및 access token 블랙리스트 등록")
    public ResponseEntity<?> logout(
            @Parameter(hidden = true) @RequestHeader("Authorization") String bearerToken,
            HttpServletResponse response) {

        authService.logout(bearerToken);
        // Refresh Token 쿠키 삭제
        ResponseCookie deleteCookie = ResponseCookie.from("refreshToken", "")
                .httpOnly(true)
                .secure(true)
                .path("/")
                .maxAge(0)  // 즉시 만료
                .sameSite("None")
                .build();
        response.addHeader(HttpHeaders.SET_COOKIE, deleteCookie.toString());

        return ApiResponse.of(HttpStatus.OK, "로그아웃 성공", null);
    }

    // refresh
    @PostMapping("/token/refresh")
    @Operation(summary = "리프레쉬", description = "refresh token으로 accesstoken 재발급")
    public ResponseEntity<?> refreshToken(
            @CookieValue(name = "refreshToken", required = false)String refreshToken) {
        if (refreshToken == null) {
            throw new UnauthorizedException("Refresh Token이 없습니다.");
        }

        String newAccessToken = authService.refresh(refreshToken);

        return ApiResponse.of(HttpStatus.OK, "토큰 재발급 성공",
                Map.of("accessToken", newAccessToken)
        );
    }

    @GetMapping("/email/exists")
    public ResponseEntity<?> checkEmail(
            @RequestParam @Email(message = "이메일 형식이 올바르지 않습니다.")
            @NotBlank(message = "이메일을 입력해주세요.") String email) {
        Boolean registered = authService.checkEmail(email);
        String message;
        if (registered) {
            message = "이미 가입된 이메일입니다";
        }
        else {
            message = "미가입된 이메일입니다";
        }
        return ApiResponse.of(HttpStatus.OK, message,
                Map.of("registered", registered)
        );
    }

    @PostMapping("/password/email/send")
    public ResponseEntity<?> sendPasswordResetCode(
            @RequestBody @Valid EmailSendRequest request) {
        EmailSendResponse emailSendResponse = authService.sendPasswordResetCode(request.getEmail());
        return ApiResponse.of(HttpStatus.OK, "인증코드를 발송했습니다.", emailSendResponse);
    }

    // 비밀번호 재설정
    @PostMapping("/password/reset")
    public ResponseEntity<?> resetPassword(
            @RequestBody @Valid PasswordResetRequest request) {
        authService.resetPassword(request.getEmail(), request.getNewPassword());
        return ApiResponse.of(HttpStatus.OK, "비밀번호가 변경됐습니다.", null);
    }




    // 쿠키 생성 공통 메서드
    private ResponseCookie createRefreshTokenCookie(String refreshToken) {
        return ResponseCookie.from("refreshToken", refreshToken)
                .httpOnly(true)
                .secure(true)
                .path("/")
                .maxAge(Duration.ofDays(7))
                .sameSite("None")
                .build();
    }
}