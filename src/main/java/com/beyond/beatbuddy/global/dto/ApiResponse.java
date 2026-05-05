package com.beyond.beatbuddy.global.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

@Getter
@AllArgsConstructor
@Builder
public class ApiResponse<T> {
    private int status;
    private String message;
    private T result;

    public static <T> ResponseEntity<ApiResponse<T>> of(
            HttpStatus httpStatus, String message, T data) {
        ApiResponse<T> body = ApiResponse.<T>builder()
                .status(httpStatus.value())
                .message(message)
                .result(data)
                .build();
        return ResponseEntity.status(httpStatus).body(body);
    }
}