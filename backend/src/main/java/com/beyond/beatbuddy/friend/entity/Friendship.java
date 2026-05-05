package com.beyond.beatbuddy.friend.entity;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Friendship {
    private Long friendshipId;
    private Long requesterId;
    private Long receiverId;
    private Long groupId;
    private String status; // "PENDING" | "ACCEPTED"
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
