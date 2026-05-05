package com.beyond.beatbuddy.chat.dto.request;

import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
public class ChatMessageRequest {
    private Long roomId;
    private String messageText;
}
