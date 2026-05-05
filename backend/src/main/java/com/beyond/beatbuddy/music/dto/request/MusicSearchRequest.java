package com.beyond.beatbuddy.music.dto.request;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

// 사용자가 검색창에 입력한 검색어를 서버로 보냄
// MusicController에서 검색 요청 받음
// MusicService로 전달 & Spotify API 호출의 입력값
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class MusicSearchRequest {

    @NotBlank(message = "검색어는 필수입니다.")
    private String keyword;
}
