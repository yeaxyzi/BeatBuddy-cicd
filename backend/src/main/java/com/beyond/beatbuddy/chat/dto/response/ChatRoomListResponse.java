package com.beyond.beatbuddy.chat.dto.response;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class ChatRoomListResponse {
    private Long roomId;
    private Long opponentUserId;
    private String opponentNickname;
    private String opponentProfileImageUrl;
    private String lastMessageText;
    private LocalDateTime lastMessageAt;
    private Integer unreadCount;
}
