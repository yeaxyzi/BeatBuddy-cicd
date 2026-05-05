package com.beyond.beatbuddy.global.exception;

import org.springframework.http.HttpStatus;
// 409 - 중복 (이메일, 닉네임)
public class ConflictException extends BusinessException {
    public ConflictException(String message) {
        super(HttpStatus.CONFLICT, message);
    }
}