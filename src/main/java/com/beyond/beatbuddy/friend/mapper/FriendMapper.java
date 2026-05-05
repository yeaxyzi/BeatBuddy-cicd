package com.beyond.beatbuddy.friend.mapper;

import com.beyond.beatbuddy.friend.dto.FriendDetailResponse;
import com.beyond.beatbuddy.friend.dto.FriendResponse;
import com.beyond.beatbuddy.friend.entity.Friendship;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface FriendMapper {

        // 친구 요청 생성
        void insertRequest(Friendship friendship);

        // 중복/역방향 요청 확인 (requester-receiver 양방향)
        Friendship findPendingRequest(@Param("requesterId") Long requesterId,
                        @Param("receiverId") Long receiverId);

        // 이미 친구 상태인지 확인
        Friendship findAcceptedFriendship(@Param("userA") Long userA,
                        @Param("userB") Long userB);

        // 특정 friendship 조회 (수락/거절 처리용)
        Friendship findById(@Param("friendshipId") Long friendshipId);

        // 상태 업데이트 (ACCEPTED / 거절은 삭제)
        void updateStatus(@Param("friendshipId") Long friendshipId,
                        @Param("status") String status);

        // 친구 삭제
        void deleteFriend(@Param("friendshipId") Long friendshipId);

        // 내 친구 목록 (양방향: 내가 요청자이거나 수신자인 ACCEPTED 목록)
        List<FriendResponse> findFriendsByUserId(@Param("userId") Long userId);

        // 내가 받은 친구 요청 목록 (PENDING, 내가 receiver인 것)
        List<FriendResponse> findReceivedRequests(@Param("userId") Long userId);

        // 친구 상세 조회 (상대방 프로필 + 최애곡 목록)
        FriendDetailResponse findFriendDetail(@Param("myUserId") Long myUserId,
                        @Param("friendId") Long friendId);
}
