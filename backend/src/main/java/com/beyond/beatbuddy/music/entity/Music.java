package com.beyond.beatbuddy.music.entity;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

// 곡 기본 정보
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Music {

    private String musicId;
    private String albumId;
    private String trackName;
    private String artistName;
}
