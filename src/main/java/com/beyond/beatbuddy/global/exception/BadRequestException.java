package com.beyond.beatbuddy.global.exception;

import org.springframework.http.HttpStatus;
// 400 - 형식은 맞는데 내용이 잘못됨
public class BadRequestException extends BusinessException {
    public BadRequestException(String message) {
        super(HttpStatus.BAD_REQUEST, message);
    }
}
