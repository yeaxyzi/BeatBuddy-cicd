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
public class ChatRoom {
    private Long roomId;
    private Long userAId;
    private Long userBId;
    private Long groupId;
    private Long lastMessageId;
    private String lastMessageText;
    private LocalDateTime lastMessageAt;
    private LocalDateTime createdAt;
}
