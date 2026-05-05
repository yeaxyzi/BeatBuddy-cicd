package com.beyond.beatbuddy.user.service;

import org.springframework.beans.factory.annotation.Value;
import com.beyond.beatbuddy.global.entity.User;
import com.beyond.beatbuddy.global.exception.BadRequestException;
import com.beyond.beatbuddy.global.exception.ConflictException;
import com.beyond.beatbuddy.global.exception.NotFoundException;
import com.beyond.beatbuddy.global.exception.UnauthorizedException;
import com.beyond.beatbuddy.global.util.JwtUtil;
import com.beyond.beatbuddy.global.util.RedisService;
import com.beyond.beatbuddy.user.dto.request.ChangePasswordRequest;
import com.beyond.beatbuddy.user.dto.request.UpdateChatNotificationRequest;
import com.beyond.beatbuddy.user.dto.request.UpdateGroupNicknameRequest;
import com.beyond.beatbuddy.user.dto.request.UpdateSocialNotificationRequest;
import com.beyond.beatbuddy.user.dto.response.UserGroupNicknameListResponse;
import com.beyond.beatbuddy.user.dto.response.UserNotificationSettingResponse;
import com.beyond.beatbuddy.user.dto.response.UserProfileResponse;
import com.beyond.beatbuddy.user.mapper.MyPageMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import com.beyond.beatbuddy.global.util.FileStorageService;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;



@Service
@RequiredArgsConstructor

public class UserServiceImpl implements UserService {
    @Value("${file.upload.profile}")
    private String profileUploadDir;
    private final MyPageMapper myPageMapper;
    private final PasswordEncoder passwordEncoder;
    private final JwtUtil jwtUtil;
    private final RedisService redisService;
    private final FileStorageService fileStorageService;


    @Override
    public UserProfileResponse getMyProfile(String email) {
        User user = getActiveUserByEmail(email);
        return new UserProfileResponse(user);
    }

    @Override
    public UserNotificationSettingResponse getMyNotificationSetting(String email) {
        User user = getActiveUserByEmail(email);
        return myPageMapper.selectNotificationSetting(user.getUserId());
    }

    @Override
    public void updateChatNotificationSetting(String email, UpdateChatNotificationRequest request) {
        User user = getActiveUserByEmail(email);
        myPageMapper.updateChatNotificationSetting(user.getUserId(), request.getAllowPushChat());
    }

    @Override
    public void updateSocialNotificationSetting(String email, UpdateSocialNotificationRequest request) {
        User user = getActiveUserByEmail(email);
        myPageMapper.updateSocialNotificationSetting(user.getUserId(), request.getAllowPushSocial());
    }

    @Override
    public UserGroupNicknameListResponse getMyGroupNicknames(String email) {
        User user = getActiveUserByEmail(email);
        return new UserGroupNicknameListResponse(myPageMapper.selectMyGroupNicknames(user.getUserId()));
    }

    @Override
    public void changePassword(String email, ChangePasswordRequest request) {
        User user = getActiveUserByEmail(email);

        if (!passwordEncoder.matches(request.getCurrentPassword(), user.getPassword())) {
            throw new UnauthorizedException("현재 비밀번호가 일치하지 않습니다.");
        }

        if (!request.getNewPassword().equals(request.getNewPasswordConfirm())) {
            throw new BadRequestException("새 비밀번호와 비밀번호 확인이 일치하지 않습니다.");
        }

        if (passwordEncoder.matches(request.getNewPassword(), user.getPassword())) {
            throw new ConflictException("현재 비밀번호와 동일한 비밀번호로 변경할 수 없습니다.");
        }

        String encodedPassword = passwordEncoder.encode(request.getNewPassword());
        myPageMapper.updatePassword(user.getUserId(), encodedPassword);
    }

    @Override
    public void updateGroupNickname(String email, Long groupId, UpdateGroupNicknameRequest request) {
        User user = getActiveUserByEmail(email);

        int duplicateCount = myPageMapper.countDuplicateGroupNickname(
                groupId,
                user.getUserId(),
                request.getGroupNickname()
        );

        if (duplicateCount > 0) {
            throw new ConflictException("이미 사용 중인 그룹 닉네임입니다.");
        }

        int updatedRows = myPageMapper.updateGroupNickname(
                user.getUserId(),
                groupId,
                request.getGroupNickname()
        );

        if (updatedRows == 0) {
            throw new NotFoundException("해당 그룹 멤버 정보를 찾을 수 없습니다.");
        }
    }

    @Override
    public void updateProfileImage(String email, String profileImageUrl) {
        User user = getActiveUserByEmail(email);
        String oldProfileImageUrl = user.getProfileImageUrl();

        myPageMapper.updateProfileImage(user.getUserId(), profileImageUrl);

        deleteOldProfileImage(oldProfileImageUrl, profileImageUrl);
    }



    private void deleteOldProfileImage(String oldProfileImageUrl, String newProfileImageUrl) {
        if (oldProfileImageUrl == null || oldProfileImageUrl.isBlank()) {
            return;
        }

        if (oldProfileImageUrl.equals(newProfileImageUrl)) {
            return;
        }

        if (oldProfileImageUrl.equals("/default-profile.jpg")) {
            return;
        }

        if (!oldProfileImageUrl.startsWith("/images/profiles/")) {
            return;
        }

        String filename = oldProfileImageUrl.substring("/images/profiles/".length());
        File oldFile = new File(new File(profileUploadDir).getAbsoluteFile(), filename);

        try {
            Files.deleteIfExists(oldFile.toPath());
        } catch (IOException e) {
            throw new RuntimeException("기존 프로필 이미지 삭제에 실패했습니다.", e);
        }
    }



    @Override
    public void withdraw(String email, String bearerToken) {
        User user = getActiveUserByEmail(email);

        myPageMapper.withdrawUser(user.getUserId());
        redisService.deleteRefreshToken(user.getUserId());

        String accessToken = jwtUtil.substringToken(bearerToken);
        long expiration = jwtUtil.getExpiration(accessToken);

        if (expiration > 0) {
            redisService.addBlacklist(accessToken, expiration);
        }
    }

    private User getActiveUserByEmail(String email) {
        return myPageMapper.selectUserByEmail(email)
                .orElseThrow(() -> new NotFoundException("사용자를 찾을 수 없습니다."));
    }
}
