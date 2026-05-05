package com.beyond.beatbuddy.notification.service;

import com.beyond.beatbuddy.global.exception.ForbiddenException;
import com.beyond.beatbuddy.global.exception.NotFoundException;
import com.beyond.beatbuddy.notification.dto.NotificationResponse;
import com.beyond.beatbuddy.notification.entity.Notification;
import com.beyond.beatbuddy.notification.mapper.NotificationMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class NotificationService {

    private final NotificationMapper notificationMapper;

    /**
     * 알림 목록 조회 (NOTI_002)
     * - 본인에게 온 알림만 조회 가능 (최신순)
     */
    public List<NotificationResponse> getMyNotifications(Long myUserId) {
        return notificationMapper.findAllByUserId(myUserId).stream()
                .map(n -> NotificationResponse.builder()
                        .notificationId(n.getNotificationId())
                        .senderId(n.getSenderId())
                        .groupId(n.getGroupId())
                        .groupName(n.getGroupName())
                        .groupNickname(n.getGroupNickname())
                        .targetId(n.getTargetId())
                        .type(n.getType())
                        .message(n.getMessage())
                        .isRead(n.isRead())
                        .createdAt(n.getCreatedAt())
                        .build())
                .collect(Collectors.toList());
    }

    /**
     * 알림 읽음 처리 (NOTI_001)
     * - 본인의 알림만 읽음 처리 가능
     * - 이미 읽은 알림이어도 멱등성 보장 (중복 호출해도 200 반환)
     */
    @Transactional
    public void markAsRead(Long myUserId, Long notificationId) {
        Notification notification = notificationMapper.findById(notificationId);

        if (notification == null) {
            throw new NotFoundException("요청하신 자원을 찾을 수 없습니다.");
        }
        if (!notification.getUserId().equals(myUserId)) {
            throw new ForbiddenException("해당 리소스에 접근할 권한이 없습니다.");
        }

        notificationMapper.markAsRead(notificationId);
    }

    /**
     * 알림 단일 삭제 (NOTI_003)
     */
    @Transactional
    public void deleteNotification(Long myUserId, Long notificationId) {
        Notification notification = notificationMapper.findById(notificationId);

        if (notification == null) {
            throw new NotFoundException("요청하신 자원을 찾을 수 없습니다.");
        }
        if (!notification.getUserId().equals(myUserId)) {
            throw new ForbiddenException("해당 리소스에 접근할 권한이 없습니다.");
        }

        notificationMapper.deleteNotification(notificationId);
    }
}
