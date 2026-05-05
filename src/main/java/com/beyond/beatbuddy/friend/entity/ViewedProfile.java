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
public class ViewedProfile {
    private Long id;
    private Long viewerId;
    private Long viewedId;
    private LocalDateTime viewedAt;
}
