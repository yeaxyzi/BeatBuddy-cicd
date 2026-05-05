package com.beyond.beatbuddy.user.dto.request;

import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
public class UpdateChatNotificationRequest {

    @NotNull(message = "채팅 알림 설정값은 필수입니다.")
    private Boolean allowPushChat;
}
