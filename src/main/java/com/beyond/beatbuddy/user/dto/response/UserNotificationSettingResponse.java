package com.beyond.beatbuddy.user.dto.response;

import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
public class UserNotificationSettingResponse {
    private Boolean allowPushChat;
    private Boolean allowPushSocial;
}