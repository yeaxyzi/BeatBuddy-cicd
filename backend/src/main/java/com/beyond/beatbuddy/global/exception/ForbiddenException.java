package com.beyond.beatbuddy.global.exception;

import org.springframework.http.HttpStatus;
// 403 - 인증은 됐는데 권한이 없음 admin 이런거
public class ForbiddenException extends BusinessException {
    public ForbiddenException(String message) {
        super(HttpStatus.FORBIDDEN, message);
    }
}
