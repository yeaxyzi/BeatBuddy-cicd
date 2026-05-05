package com.beyond.beatbuddy.auth.dto.response;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class EmailVerifyResponse {
    private boolean verified;
    private int attempts;
    private int maxAttempts;
}
