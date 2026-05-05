package com.beyond.beatbuddy.chat.dto.response;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
@AllArgsConstructor
public class ChatRoomResponse {
    private Long roomId;
    private Long opponentUserId;
    private String opponentNickname;
    private String opponentProfileImageUrl;
}