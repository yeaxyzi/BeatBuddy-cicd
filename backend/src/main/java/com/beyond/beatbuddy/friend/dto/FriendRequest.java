package com.beyond.beatbuddy.friend.dto;

import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
public class FriendRequest {
    @NotNull(message = "그룹 ID는 필수입니다.")
    private Long groupId;

    @NotNull(message = "요청 대상 ID는 필수입니다.")
    private Long receiverId;
}
