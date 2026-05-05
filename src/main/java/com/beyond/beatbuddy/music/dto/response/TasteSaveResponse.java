package com.beyond.beatbuddy.music.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

// 취향곡 저장 또는 수정이 성공했을 때 서버가 성공 결과를 내려줌
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TasteSaveResponse {

    private Long userId;
    private int savedCount;       // 기본 10
    private boolean success;     // 저장 성공 여부
    private String message;     // "취향 저장 완료" 같은 안내용
}
