package com.beyond.beatbuddy.notification.entity;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Notification {
    private Long notificationId;
    private Long userId; // 알림 수신자
    private Long senderId; // 알림 발신자
    private Long groupId;
    private String groupName;
    private String groupNickname;
    private String type; // FRIEND_REQUEST | FRIEND_ACCEPT | TOTAL_SYSTEM
    private Long targetId; // room_id 또는 group_id (선택)
    private String message;
    private boolean isRead;
    private LocalDateTime createdAt;
}
