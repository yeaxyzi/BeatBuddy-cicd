package com.beyond.beatbuddy.global.exception;

import org.springframework.http.HttpStatus;
// 401 - 인증 안 됨 ex) "너 누구야? 로그인해"
public class UnauthorizedException extends BusinessException {
    public UnauthorizedException(String message) {
        super(HttpStatus.UNAUTHORIZED, message);
    }
}
