package com.beyond.beatbuddy.user.service;

import com.beyond.beatbuddy.user.dto.request.ChangePasswordRequest;
import com.beyond.beatbuddy.user.dto.request.UpdateChatNotificationRequest;
import com.beyond.beatbuddy.user.dto.request.UpdateGroupNicknameRequest;
import com.beyond.beatbuddy.user.dto.request.UpdateSocialNotificationRequest;
import com.beyond.beatbuddy.user.dto.response.UserGroupNicknameListResponse;
import com.beyond.beatbuddy.user.dto.response.UserNotificationSettingResponse;
import com.beyond.beatbuddy.user.dto.response.UserProfileResponse;


public interface UserService {
    UserProfileResponse getMyProfile(String email);

    UserNotificationSettingResponse getMyNotificationSetting(String email);

    void updateChatNotificationSetting(String email, UpdateChatNotificationRequest request);

    void updateSocialNotificationSetting(String email, UpdateSocialNotificationRequest request);

    UserGroupNicknameListResponse getMyGroupNicknames(String email);

    void changePassword(String email, ChangePasswordRequest request);

    void updateGroupNickname(String email, Long groupId, UpdateGroupNicknameRequest request);

    void updateProfileImage(String email, String profileImageUrl);

    void withdraw(String email, String bearerToken);
}
