package com.beyond.beatbuddy.auth.dto.response;

import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class TokenResponse {
    private String accessToken;
    @JsonIgnore
    private String refreshToken;
    private Long userId;
    private String email;
    private String nickname;
}
