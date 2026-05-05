package com.beyond.beatbuddy.recommendation.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.List;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RecommendationResponseDto {
    private Long userId;
    private String nickname;
    private String groupNickname;
    private String profileImageUrl;
    private String gender;
    private Integer birthYear;
    // 취향 유사도 점수 (낮을수록 유사: VEC_DISTANCE 결과값)
    private Double similarityScore;
    private List<FavoriteMusicItem> favoriteMusicList;

    @Getter
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class FavoriteMusicItem {
        private String musicId;
        private String trackName;
        private String artistName;
        private String albumName;
        private String albumCoverUrl;
    }
}
