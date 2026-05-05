package com.beyond.beatbuddy.chat.dto.request;

import lombok.Getter;

@Getter
public class CreateChatRoomRequest {

    private Long opponentUserId;
    private Long groupId;

}