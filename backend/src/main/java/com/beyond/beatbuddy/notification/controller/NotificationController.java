package com.beyond.beatbuddy.notification.controller;

import com.beyond.beatbuddy.global.dto.ApiResponse;
import com.beyond.beatbuddy.notification.dto.NotificationResponse;
import com.beyond.beatbuddy.notification.service.NotificationService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import com.beyond.beatbuddy.global.security.UserPrincipal;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Tag(name = "알림", description = "알림 API")
@RestController
@RequestMapping("/api/v1/notifications")
@RequiredArgsConstructor
public class NotificationController {

    private final NotificationService notificationService;

    /** NOTI_002 - 내 알림 목록 조회 */
    @Operation(summary = "알림 목록 조회")
    @GetMapping
    public ResponseEntity<ApiResponse<List<NotificationResponse>>> getMyNotifications(
            @AuthenticationPrincipal UserPrincipal userPrincipal) {
        Long myUserId = userPrincipal.getUserId();
        List<NotificationResponse> result = notificationService.getMyNotifications(myUserId);
        return ApiResponse.of(HttpStatus.OK, "알림 목록을 성공적으로 조회했습니다.", result);
    }

    /** NOTI_001 - 알림 읽음 처리 */
    @Operation(summary = "알림 읽음 처리")
    @PatchMapping("/{notificationId}/read")
    public ResponseEntity<ApiResponse<Void>> markAsRead(
            @AuthenticationPrincipal UserPrincipal userPrincipal,
            @PathVariable Long notificationId) {
        Long myUserId = userPrincipal.getUserId();
        notificationService.markAsRead(myUserId, notificationId);
        return ApiResponse.of(HttpStatus.OK, "알림 읽음 처리가 완료되었습니다.", null);
    }

    /** NOTI_003 - 알림 삭제 */
    @Operation(summary = "알림 삭제", description = "X 버튼을 눌러 알림을 영구 삭제합니다.")
    @DeleteMapping("/{notificationId}")
    public ResponseEntity<ApiResponse<Void>> deleteNotification(
            @AuthenticationPrincipal UserPrincipal userPrincipal,
            @PathVariable Long notificationId) {
        Long myUserId = userPrincipal.getUserId();
        notificationService.deleteNotification(myUserId, notificationId);
        return ApiResponse.of(HttpStatus.OK, "알림을 성공적으로 삭제했습니다.", null);
    }
}
