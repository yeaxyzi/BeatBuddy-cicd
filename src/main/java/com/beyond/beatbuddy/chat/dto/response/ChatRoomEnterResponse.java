package com.beyond.beatbuddy.chat.dto.response;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.List;

@Getter
@NoArgsConstructor
@AllArgsConstructor
public class ChatRoomEnterResponse {
    private List<ChatMessageResponse> messages;
    private boolean isOpponentExited;
}
