package com.beyond.beatbuddy.user.controller;

import com.beyond.beatbuddy.global.dto.ApiResponse;
import com.beyond.beatbuddy.global.security.UserPrincipal;
import com.beyond.beatbuddy.user.dto.request.ChangePasswordRequest;
import com.beyond.beatbuddy.user.dto.request.UpdateChatNotificationRequest;
import com.beyond.beatbuddy.user.dto.request.UpdateGroupNicknameRequest;
import com.beyond.beatbuddy.user.dto.request.UpdateSocialNotificationRequest;
import com.beyond.beatbuddy.user.dto.response.UserGroupNicknameListResponse;
import com.beyond.beatbuddy.user.dto.response.UserNotificationSettingResponse;
import com.beyond.beatbuddy.user.dto.response.UserProfileResponse;
import com.beyond.beatbuddy.user.service.UserService;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseCookie;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import com.beyond.beatbuddy.global.util.FileStorageService;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/users")
public class UserController {

    private final UserService userService;
    private final FileStorageService fileStorageService;

    @GetMapping("/me")
    public ResponseEntity<ApiResponse<UserProfileResponse>> getMyProfile(
            @AuthenticationPrincipal UserPrincipal userPrincipal) {
        return ApiResponse.of(
                HttpStatus.OK,
                "내 프로필 조회가 완료되었습니다.",
                userService.getMyProfile(userPrincipal.getEmail())
        );
    }

    @GetMapping("/me/notification")
    public ResponseEntity<ApiResponse<UserNotificationSettingResponse>> getMyNotificationSetting(
            @AuthenticationPrincipal UserPrincipal userPrincipal) {
        return ApiResponse.of(
                HttpStatus.OK,
                "알림 설정 조회가 완료되었습니다.",
                userService.getMyNotificationSetting(userPrincipal.getEmail())
        );
    }

    @PatchMapping("/me/notifications/chat")
    public ResponseEntity<ApiResponse<Void>> updateChatNotificationSetting(
            @AuthenticationPrincipal UserPrincipal userPrincipal,
            @Valid @RequestBody UpdateChatNotificationRequest request) {
        userService.updateChatNotificationSetting(userPrincipal.getEmail(), request);
        return ApiResponse.of(HttpStatus.OK, "채팅 알림 설정이 변경되었습니다.", null);
    }

    @PatchMapping("/me/notifications/social")
    public ResponseEntity<ApiResponse<Void>> updateSocialNotificationSetting(
            @AuthenticationPrincipal UserPrincipal userPrincipal,
            @Valid @RequestBody UpdateSocialNotificationRequest request) {
        userService.updateSocialNotificationSetting(userPrincipal.getEmail(), request);
        return ApiResponse.of(HttpStatus.OK, "소셜 알림 설정이 변경되었습니다.", null);
    }

    @GetMapping("/me/group-nicknames")
    public ResponseEntity<ApiResponse<UserGroupNicknameListResponse>> getMyGroupNicknames(
            @AuthenticationPrincipal UserPrincipal userPrincipal) {
        return ApiResponse.of(
                HttpStatus.OK,
                "그룹 닉네임 목록 조회가 완료되었습니다.",
                userService.getMyGroupNicknames(userPrincipal.getEmail())
        );
    }

    @PatchMapping("/me/group-nicknames/{groupId}")
    public ResponseEntity<ApiResponse<Void>> updateGroupNickname(
            @AuthenticationPrincipal UserPrincipal userPrincipal,
            @PathVariable Long groupId,
            @Valid @RequestBody UpdateGroupNicknameRequest request) {
        userService.updateGroupNickname(userPrincipal.getEmail(), groupId, request);
        return ApiResponse.of(HttpStatus.OK, "그룹 닉네임이 변경되었습니다.", null);
    }

    @PatchMapping("/password")
    public ResponseEntity<ApiResponse<Void>> changePassword(
            @AuthenticationPrincipal UserPrincipal userPrincipal,
            @Valid @RequestBody ChangePasswordRequest request) {
        userService.changePassword(userPrincipal.getEmail(), request);
        return ApiResponse.of(HttpStatus.OK, "비밀번호가 변경되었습니다.", null);
    }

    @PatchMapping(value = "/me/profile-image", consumes = "multipart/form-data")
    public ResponseEntity<ApiResponse<Void>> updateProfileImage(
            @AuthenticationPrincipal UserPrincipal userPrincipal,
            @RequestPart(value = "profileImage", required = false) MultipartFile profileImage) {

        System.out.println("profileImage null 여부 = " + (profileImage == null));
        System.out.println("profileImage isEmpty = " + (profileImage != null && profileImage.isEmpty()));
        System.out.println("profileImage originalFilename = " + (profileImage != null ? profileImage.getOriginalFilename() : "null"));

        String profileImageUrl = fileStorageService.saveProfileImage(profileImage);

        System.out.println("저장된 profileImageUrl = " + profileImageUrl);

        userService.updateProfileImage(userPrincipal.getEmail(), profileImageUrl);

        return ApiResponse.of(HttpStatus.OK, "프로필 이미지가 변경되었습니다.", null);
    }

    @DeleteMapping("/me/profile-image")
    public ResponseEntity<ApiResponse<Void>> deleteProfileImage(
            @AuthenticationPrincipal UserPrincipal userPrincipal) {

        userService.updateProfileImage(userPrincipal.getEmail(), "/default-profile.jpg");

        return ApiResponse.of(HttpStatus.OK, "프로필 이미지가 기본 이미지로 변경되었습니다.", null);
    }


    @DeleteMapping("/me")
    public ResponseEntity<ApiResponse<Void>> withdraw(
            @AuthenticationPrincipal UserPrincipal userPrincipal,
            @RequestHeader("Authorization") String bearerToken,
            HttpServletResponse response) {
        userService.withdraw(userPrincipal.getEmail(), bearerToken);

        ResponseCookie deleteCookie = ResponseCookie.from("refreshToken", "")
                .httpOnly(true)
                .secure(true)
                .path("/")
                .maxAge(0)
                .sameSite("None")
                .build();
        response.addHeader(HttpHeaders.SET_COOKIE, deleteCookie.toString());

        return ApiResponse.of(HttpStatus.OK, "회원 탈퇴가 완료되었습니다.", null);
    }
}