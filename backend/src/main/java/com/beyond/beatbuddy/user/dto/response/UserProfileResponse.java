package com.beyond.beatbuddy.user.dto.response;

import com.beyond.beatbuddy.global.entity.User;
import lombok.Getter;

@Getter
public class UserProfileResponse {
    private final Long userId;
    private final String email;
    private final String nickname;
    private final String gender;
    private final Integer birthYear;
    private final String profileImageUrl;

    public UserProfileResponse(User user) {
        this.userId = user.getUserId();
        this.email = user.getEmail();
        this.nickname = user.getNickname();
        this.gender = user.getGender();
        this.birthYear = user.getBirthYear();
        this.profileImageUrl = user.getProfileImageUrl();
    }
}