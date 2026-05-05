package com.beyond.beatbuddy.music.entity;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

// 유저가 고른 곡을 저장하는 연결 역할
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UserFavMusic {

    private Long favId;
    private Long userId;
    private String musicId;
    private LocalDateTime createdAt;
}
