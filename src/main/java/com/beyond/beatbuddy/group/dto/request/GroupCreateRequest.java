package com.beyond.beatbuddy.group.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
@AllArgsConstructor
public class GroupCreateRequest {

    @NotBlank(message = "그룹명은 필수입니다.")
    @Size(max = 20, message = "그룹명은 최대 20자까지 가능합니다.")
    private String groupName;

    @Size(max = 50, message = "한 줄 소개는 최대 50자까지 가능합니다.")
    private String description;

    @NotBlank(message = "초대 코드는 필수입니다.")
    @Size(max = 20, message = "초대 코드는 최대 20자까지 가능합니다.")
    @Pattern(
            regexp = "^[A-Z0-9]+$",
            message = "초대코드는 영문 대문자와 숫자만 가능합니다."
    )
    private String inviteCode;

    @Size(max = 20, message = "닉네임은 최대 20자까지 가능합니다.")
    private String groupNickname;
}
