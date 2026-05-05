package com.beyond.beatbuddy.recommendation.service;

import com.beyond.beatbuddy.global.util.RedisService;
import com.beyond.beatbuddy.group.mapper.GroupMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class RecommendationCacheService {

    private static final String PREFIX = "recommendation:list:";

    private final RedisService redisService;
    private final GroupMapper groupMapper;

    public String cacheKey(Long myUserId, Long groupId) {
        return PREFIX + myUserId + ":" + groupId;
    }

    public void evictUserGroup(Long myUserId, Long groupId) {
        redisService.deleteKey(cacheKey(myUserId, groupId));
    }

    public void evictUser(Long userId) {
        redisService.deleteKeysByPattern(PREFIX + userId + ":*");
    }

    public void evictGroup(Long groupId) {
        redisService.deleteKeysByPattern(PREFIX + "*:" + groupId);
    }

    public void evictUserAndGroups(Long userId) {
        evictUser(userId);
        groupMapper.findGroupsByUserId(userId).forEach(group -> evictGroup(group.getGroupId()));
    }

    public void evictUsers(Long userA, Long userB) {
        evictUser(userA);
        evictUser(userB);
    }
}
