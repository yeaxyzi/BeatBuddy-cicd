package com.beyond.beatbuddy.recommendation.mapper;

import com.beyond.beatbuddy.recommendation.dto.RecommendationResponseDto;
import com.beyond.beatbuddy.friend.entity.ViewedProfile;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface RecommendationMapper {

    // 스킵(열람) 이력 저장 → viewed_profiles
    void insertViewedProfile(ViewedProfile viewedProfile);

    // 내 user_profiles 존재 여부 확인
    boolean existsUserProfile(@Param("userId") Long userId);

    // 그룹 내 취향 유사도 기반 추천 목록 조회
    List<RecommendationResponseDto> findRecommendations(
            @Param("myUserId") Long myUserId,
            @Param("groupId") Long groupId);
}
