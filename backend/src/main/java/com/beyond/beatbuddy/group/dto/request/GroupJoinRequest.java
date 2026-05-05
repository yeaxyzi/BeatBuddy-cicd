package com.beyond.beatbuddy.group.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
@AllArgsConstructor
public class GroupJoinRequest {

    @NotBlank(message = "초대코드는 필수입니다.")
    private String inviteCode;

    @Size(max = 20, message = "닉네임은 최대 20자까지 가능합니다.")
    private String groupNickname;
}
