package com.beyond.beatbuddy.music.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

// Spotify 검색 결과 1곡에 대한 정보를 프론트에 내려줌
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class MusicSearchResponse {

    private String trackId;
    private String trackName;
    private String artistName;
    private String albumId;
    private String albumName;
    private String albumCoverUrl;
}
