package com.beyond.beatbuddy.friend.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.List;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class FriendDetailResponse {
    private Long friendId;
    private String nickname;
    private String profileImageUrl;
    private String gender;
    private Integer birthYear;
    private Long roomId;
    // 최애곡 목록 (상세 정보 포함)
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
