package com.beyond.beatbuddy.notification.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class NotificationResponse {
    private Long notificationId;
    private Long senderId;
    private Long groupId;
    private String groupName;
    private String groupNickname;
    private Long targetId;
    private String type; // FRIEND_REQUEST | FRIEND_ACCEPT | TOTAL_SYSTEM
    private String message;
    private boolean isRead;
    private LocalDateTime createdAt;
}
