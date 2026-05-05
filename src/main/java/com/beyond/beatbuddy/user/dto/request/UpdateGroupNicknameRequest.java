package com.beyond.beatbuddy.user.dto.request;

import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
public class UpdateGroupNicknameRequest {

    @NotBlank(message = "그룹 닉네임을 입력해주세요.")
    private String groupNickname;
}