package com.beyond.beatbuddy.notification.mapper;

import com.beyond.beatbuddy.notification.entity.Notification;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface NotificationMapper {

    // 알림 단건 조회
    Notification findById(@Param("notificationId") Long notificationId);

    // 내 알림 목록 조회
    List<Notification> findAllByUserId(@Param("userId") Long userId);

    // 읽음 처리 (특정 알림ID)
    void markAsRead(@Param("notificationId") Long notificationId);

    // 알림 단일 삭제 (X 버튼 용)
    void deleteNotification(@Param("notificationId") Long notificationId);

    // 알림 복합 삭제 (수락/거절 시 해당 요청 알림 삭제)
    void deleteRequest(@Param("userId") Long userId,
                       @Param("senderId") Long senderId,
                       @Param("groupId") Long groupId,
                       @Param("type") String type);

    // 알림 생성 (FRIEND_003 등)
    void insertNotification(Notification notification);
}
