package com.beyond.beatbuddy.recommendation.controller;

import com.beyond.beatbuddy.recommendation.dto.RecommendationResponseDto;
import com.beyond.beatbuddy.recommendation.service.RecommendationService;
import com.beyond.beatbuddy.global.dto.ApiResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import com.beyond.beatbuddy.global.security.UserPrincipal;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Tag(name = "친구 추천", description = "그룹 내 취향 기반 친구 추천 API")
@RestController
@RequestMapping("/api/v1/groups")
@RequiredArgsConstructor
public class RecommendationController {

    private final RecommendationService recommendationService;

    /** GROUP_005 - 취향 기반 친구 추천 목록 조회 */
    @Operation(summary = "취향 기반 친구 추천 목록 조회")
    @GetMapping("/{groupId}/recommendations")
    public ResponseEntity<ApiResponse<List<RecommendationResponseDto>>> getRecommendations(
            @AuthenticationPrincipal UserPrincipal userPrincipal,
            @PathVariable Long groupId) {
        Long myUserId = userPrincipal.getUserId();
        List<RecommendationResponseDto> result = recommendationService.getRecommendations(myUserId, groupId);
        return ApiResponse.of(HttpStatus.OK, "추천 친구 목록 조회 성공", result);
    }

    /** GROUP_006 - 친구 추천 스킵 */
    @Operation(summary = "친구 추천 스킵")
    @PostMapping("/{groupId}/recommendations/{userId}/skip")
    public ResponseEntity<ApiResponse<Void>> skipRecommendation(
            @AuthenticationPrincipal UserPrincipal userPrincipal,
            @PathVariable Long groupId,
            @PathVariable Long userId) {
        Long myUserId = userPrincipal.getUserId();
        recommendationService.skipRecommendation(myUserId, groupId, userId);
        return ApiResponse.of(HttpStatus.OK, "해당 사용자를 추천에서 제외했습니다.", null);
    }
}
