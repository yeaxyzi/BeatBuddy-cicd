package com.beyond.beatbuddy.global.exception;

import org.springframework.http.HttpStatus;
// 404 - 리소스 없음
public class NotFoundException extends BusinessException {
    public NotFoundException(String message) {
        super(HttpStatus.NOT_FOUND, message);
    }
}