package com.beyond.beatbuddy.user.dto.response;

import java.util.List;
import lombok.Getter;

@Getter
public class UserGroupNicknameListResponse {
    private final List<UserGroupNicknameItemResponse> groups;

    public UserGroupNicknameListResponse(List<UserGroupNicknameItemResponse> groups) {
        this.groups = groups;
    }
}