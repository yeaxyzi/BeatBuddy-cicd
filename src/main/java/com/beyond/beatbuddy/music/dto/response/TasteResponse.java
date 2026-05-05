package com.beyond.beatbuddy.music.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

// 사용자가 이미 저장한 취향 데이터를 조회
// 저장된 목록, 각 곡의 기본 정보를 프론트에 다시 내려 프로필 모드나 편집 모드 초기값으로 쓰임
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TasteResponse {

    private Boolean isTasteAnalyzed;
    private List<TrackInfo> tracks;

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class TrackInfo {

        private String trackId;
        private String trackName;
        private String artistName;
        private String albumId;
        private String albumName;
        private String albumCoverUrl;
    }
}
