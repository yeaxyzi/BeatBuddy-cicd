package com.beyond.beatbuddy.friend.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class FriendResponse {
    private Long friendshipId;
    private Long friendId; // 상대방 userId
    private String nickname;
    private Integer groupId;
    private String groupNickname;
    private String profileImageUrl;
    private String status; // PENDING | ACCEPTED
}
