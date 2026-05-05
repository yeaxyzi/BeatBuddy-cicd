package com.beyond.beatbuddy.music.entity;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

// 곡 기본 정보와 Rapid API에서 받은 음악 속성값을 함께 담아 music_features 저장
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class MusicFeature {

    private String musicId;
    private String albumId;
    private String trackName;
    private String artistName;

    private Long popularity;
    private Long energy;
    private Long danceability;
    private Long happiness;
    private Long acousticness;
    private Long instrumentalness;
    private Long liveness;
    private Long speechiness;
}
