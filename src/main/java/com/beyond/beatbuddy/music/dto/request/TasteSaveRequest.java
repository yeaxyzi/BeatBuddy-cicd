package com.beyond.beatbuddy.music.dto.request;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

// 사용자가 최종적으로 고른 10곡의 Spotify trackId 목록을 서버로 보냄
// 최초 저장 & 수정 저장
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TasteSaveRequest {

    @NotEmpty(message = "최소 1곡 이상 선택해야 합니다.")
    @Valid
    private List<TrackRequest> tracks;

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class TrackRequest {

        @NotBlank(message = "trackId는 필수입니다.")
        private String trackId;
    }
}
