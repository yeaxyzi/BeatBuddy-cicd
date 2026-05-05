package com.beyond.beatbuddy.chat.entity;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class ChatMessage {
    private Long messageId;
    private Long roomId;
    private Long senderId;
    private String messageText;
    private LocalDateTime createdAt;
}
