package com.beyond.beatbuddy.global.exception;

import org.springframework.http.HttpStatus;
// 429 - 요청 횟수 초과
public class TooManyRequestsException extends BusinessException {
    public TooManyRequestsException(String message) {
        super(HttpStatus.TOO_MANY_REQUESTS, message);
    }
}
