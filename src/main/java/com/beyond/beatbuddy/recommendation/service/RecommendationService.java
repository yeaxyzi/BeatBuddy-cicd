package com.beyond.beatbuddy.recommendation.service;

import com.beyond.beatbuddy.global.exception.BadRequestException;
import com.beyond.beatbuddy.global.exception.ForbiddenException;
import com.beyond.beatbuddy.global.util.RedisService;
import com.beyond.beatbuddy.group.mapper.GroupMemberMapper;
import com.beyond.beatbuddy.recommendation.dto.RecommendationResponseDto;
import com.beyond.beatbuddy.friend.entity.ViewedProfile;
import com.beyond.beatbuddy.recommendation.mapper.RecommendationMapper;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.concurrent.TimeUnit;

@Service
@RequiredArgsConstructor
public class RecommendationService {

    private static final long RECOMMENDATION_CACHE_TTL_MINUTES = 5;

    private final RecommendationMapper recommendationMapper;
    private final GroupMemberMapper groupMemberMapper;
    private final RedisService redisService;
    private final ObjectMapper objectMapper;
    private final RecommendationCacheService recommendationCacheService;

    /**
     * 그룹 내 취향 기반 친구 추천 목록 조회
     */
    @Transactional(readOnly = true)
    public List<RecommendationResponseDto> getRecommendations(Long myUserId, Long groupId) {
        validateGroupMembership(myUserId, groupId);

        if (!recommendationMapper.existsUserProfile(myUserId)) {
            throw new BadRequestException("음악 취향 분석을 먼저 완료해주세요.");
        }

        String cacheKey = recommendationCacheService.cacheKey(myUserId, groupId);
        String cached = redisService.getValue(cacheKey);

        if (cached != null) {
            try {
                return objectMapper.readValue(cached, new TypeReference<List<RecommendationResponseDto>>() {});
            } catch (JsonProcessingException ignored) {
                redisService.deleteKey(cacheKey);
            }
        }

        List<RecommendationResponseDto> recommendations = recommendationMapper.findRecommendations(myUserId, groupId);

        try {
            redisService.setValue(
                    cacheKey,
                    objectMapper.writeValueAsString(recommendations),
                    RECOMMENDATION_CACHE_TTL_MINUTES,
                    TimeUnit.MINUTES
            );
        } catch (JsonProcessingException ignored) {
            // 캐시 직렬화 실패 시 DB 결과는 그대로 반환
        }

        return recommendations;
    }

    /**
     * 추천 친구 스킵 처리
     * - viewed_profiles에 기록하여 이후 추천에서 제외
     */
    @Transactional
    public void skipRecommendation(Long myUserId, Long groupId, Long targetUserId) {
        validateGroupMembership(myUserId, groupId);

        if (myUserId.equals(targetUserId)) {
            throw new IllegalArgumentException("자기 자신을 스킵할 수 없습니다.");
        }

        ViewedProfile viewedProfile = ViewedProfile.builder()
                .viewerId(myUserId)
                .viewedId(targetUserId)
                .build();

        recommendationMapper.insertViewedProfile(viewedProfile);
        recommendationCacheService.evictUserGroup(myUserId, groupId);
    }

    private void validateGroupMembership(Long userId, Long groupId) {
        if (!groupMemberMapper.existsByGroupIdAndUserId(groupId, userId)) {
            throw new ForbiddenException("해당 그룹의 멤버만 접근할 수 있습니다.");
        }
    }
}
