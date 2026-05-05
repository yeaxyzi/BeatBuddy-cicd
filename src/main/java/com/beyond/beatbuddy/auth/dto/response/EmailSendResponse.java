package com.beyond.beatbuddy.auth.dto.response;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class EmailSendResponse {
    private int attempts;     // 현재 시도 횟수 (처음엔 0)
    private int maxAttempts;  // 최대 횟수 (5)
}