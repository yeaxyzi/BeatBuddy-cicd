package com.beyond.beatbuddy.user.dto.request;

import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
public class UpdateSocialNotificationRequest {

    @NotNull(message = "소셜 알림 설정값은 필수입니다.")
    private Boolean allowPushSocial;
}