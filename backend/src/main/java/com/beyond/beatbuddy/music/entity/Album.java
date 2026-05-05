package com.beyond.beatbuddy.music.entity;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

// 앨범 정보
// Spotify에서 앨범명/앨범 커버를 가져왔을 때 albums 테이블에 저장/조회
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Album {

    private String albumId;
    private String albumName;
    private String albumCoverUrl;
}
