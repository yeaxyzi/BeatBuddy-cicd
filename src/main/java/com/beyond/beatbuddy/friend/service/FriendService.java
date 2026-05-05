package com.beyond.beatbuddy.friend.service;

import com.beyond.beatbuddy.friend.dto.FriendDetailResponse;
import com.beyond.beatbuddy.friend.dto.FriendRequest;
import com.beyond.beatbuddy.friend.dto.FriendResponse;
import com.beyond.beatbuddy.friend.entity.Friendship;
import com.beyond.beatbuddy.friend.mapper.FriendMapper;
import com.beyond.beatbuddy.global.exception.BadRequestException;
import com.beyond.beatbuddy.global.exception.ConflictException;
import com.beyond.beatbuddy.global.exception.ForbiddenException;
import com.beyond.beatbuddy.global.exception.NotFoundException;
import com.beyond.beatbuddy.group.mapper.GroupMemberMapper;
import com.beyond.beatbuddy.notification.entity.Notification;
import com.beyond.beatbuddy.notification.mapper.NotificationMapper;
import com.beyond.beatbuddy.recommendation.service.RecommendationCacheService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class FriendService {

    private final FriendMapper friendMapper;
    private final NotificationMapper notificationMapper;
    private final GroupMemberMapper groupMemberMapper;
    private final RecommendationCacheService recommendationCacheService;

    /**
     * 친구 요청 보내기
     * - 자기 자신에게 요청 불가
     * - 이미 PENDING 요청이 있는 경우 불가 (양방향)
     * - 이미 ACCEPTED 친구인 경우 불가
     */
    @Transactional
    public void sendFriendRequest(Long requesterId, FriendRequest dto) {
        Long receiverId = dto.getReceiverId();
        Long groupId = dto.getGroupId();

        if (requesterId.equals(receiverId)) {
            throw new BadRequestException("자기 자신에게 친구 요청을 보낼 수 없습니다.");
        }

        if (!groupMemberMapper.existsByGroupIdAndUserId(groupId, requesterId)
                || !groupMemberMapper.existsByGroupIdAndUserId(groupId, receiverId)) {
            throw new ForbiddenException("같은 그룹의 멤버에게만 친구 요청을 보낼 수 있습니다.");
        }

        if (friendMapper.findPendingRequest(requesterId, receiverId) != null) {
            throw new ConflictException("이미 처리 중인 친구 요청이 존재합니다.");
        }

        if (friendMapper.findAcceptedFriendship(requesterId, receiverId) != null) {
            throw new ConflictException("이미 친구 관계입니다.");
        }

        Friendship friendship = Friendship.builder()
                .requesterId(requesterId)
                .receiverId(receiverId)
                .groupId(groupId)
                .build();
        friendMapper.insertRequest(friendship);

        // FRIEND_001: 수신자(receiverId)에게 '친구 요청이 도착했다'는 알림 발송
        Notification notification = Notification.builder()
                .userId(receiverId) // 알림 수신자 = 친구 요청 받는 사람
                .senderId(requesterId) // 알림 발신자 = 핑 날린 사람
                .groupId(groupId)
                .targetId(friendship.getFriendshipId()) // 알림 액션용 friendshipId
                .type("FRIEND_REQUEST")
                .message("새로운 친구 요청이 도착했습니다.")
                .build();
        notificationMapper.insertNotification(notification);
    }

    /**
     * 친구 요청 수락
     * - 본인이 수신자인 PENDING 요청만 수락 가능
     */
    @Transactional
    public void acceptFriendRequest(Long myUserId, Long friendshipId) {
        Friendship friendship = getFriendshipOrThrow(friendshipId);

        if (!friendship.getReceiverId().equals(myUserId)) {
            throw new ForbiddenException("해당 리소스에 접근할 권한이 없습니다.");
        }
        if (!"PENDING".equals(friendship.getStatus())) {
            throw new BadRequestException("수락할 수 없는 상태의 요청입니다.");
        }

        friendMapper.updateStatus(friendshipId, "ACCEPTED");

        // 본인(myUserId)에게 도착했던 친구 요청(FRIEND_REQUEST) 알림 삭제 처리
        notificationMapper.deleteRequest(myUserId, friendship.getRequesterId(), friendship.getGroupId(), "FRIEND_REQUEST");

        // FRIEND_003: 요청자(requesterId)에게 수락 알림 발송

        Notification notification = Notification.builder()
                .userId(friendship.getRequesterId()) // 알림 수신자 = 친구 요청을 보낸 사람
                .senderId(myUserId) // 알림 발신자 = 수락한 사람
                .groupId(friendship.getGroupId())
                .targetId(friendshipId)
                .type("FRIEND_ACCEPT")
                .message("친구 요청을 수락했습니다.")
                .build();
        notificationMapper.insertNotification(notification);

        recommendationCacheService.evictUsers(myUserId, friendship.getRequesterId());
    }

    /**
     * 친구 요청 거절
     * - 본인이 수신자인 PENDING 요청만 거절 가능
     * - 거절 시 row 자체를 삭제
     */
    @Transactional
    public void rejectFriendRequest(Long myUserId, Long friendshipId) {
        Friendship friendship = getFriendshipOrThrow(friendshipId);

        if (!friendship.getReceiverId().equals(myUserId)) {
            throw new ForbiddenException("해당 리소스에 접근할 권한이 없습니다.");
        }
        if (!"PENDING".equals(friendship.getStatus())) {
            throw new BadRequestException("거절할 수 없는 상태의 요청입니다.");
        }

        friendMapper.deleteFriend(friendshipId);

        // 본인(myUserId)에게 도착했던 친구 요청(FRIEND_REQUEST) 알림 삭제 처리
        notificationMapper.deleteRequest(myUserId, friendship.getRequesterId(), friendship.getGroupId(), "FRIEND_REQUEST");
    }

    /**
     * 내 친구 목록 조회 (ACCEPTED, 양방향)
     */
    @Transactional(readOnly = true)
    public List<FriendResponse> getMyFriends(Long userId) {
        return friendMapper.findFriendsByUserId(userId);
    }

    /**
     * 내가 받은 친구 요청 목록 (PENDING, 내가 receiver인 것)
     */
    @Transactional(readOnly = true)
    public List<FriendResponse> getReceivedRequests(Long userId) {
        return friendMapper.findReceivedRequests(userId);
    }

    /**
     * 친구 상세 정보 조회
     * - 상호 ACCEPTED 상태인 친구만 조회 가능 (403)
     * - 해당 유저/친구 관계 미존재 시 (404)
     */
    @Transactional(readOnly = true)
    public FriendDetailResponse getFriendDetail(Long myUserId, Long friendId) {
        // 상호 수락된 친구인지 먼저 확인
        if (friendMapper.findAcceptedFriendship(myUserId, friendId) == null) {
            // 유저 자체가 없는지(404) vs 친구이지만 ACCEPTED 아닌지(403) 구분
            FriendDetailResponse detail = friendMapper.findFriendDetail(myUserId, friendId);
            if (detail == null) {
                throw new NotFoundException("요청하신 자원을 찾을 수 없습니다.");
            }
            throw new ForbiddenException("해당 리소스에 접근할 권한이 없습니다.");
        }

        FriendDetailResponse detail = friendMapper.findFriendDetail(myUserId, friendId);
        if (detail == null) {
            throw new NotFoundException("요청하신 자원을 찾을 수 없습니다.");
        }
        return detail;
    }

    /**
     * 친구 삭제
     * - 나와 관련된 ACCEPTED friendship만 삭제 가능
     * - 없거나 친구 아닌 경우 404
     */
    @Transactional
    public void deleteFriend(Long myUserId, Long friendshipId) {
        Friendship friendship = getFriendshipOrThrow(friendshipId);

        boolean isParticipant = friendship.getRequesterId().equals(myUserId)
                || friendship.getReceiverId().equals(myUserId);
        if (!isParticipant) {
            throw new ForbiddenException("해당 리소스에 접근할 권한이 없습니다.");
        }

        if (!"ACCEPTED".equals(friendship.getStatus())) {
            throw new NotFoundException("요청하신 자원을 찾을 수 없습니다.");
        }

        Long opponentUserId = friendship.getRequesterId().equals(myUserId)
                ? friendship.getReceiverId()
                : friendship.getRequesterId();

        friendMapper.deleteFriend(friendshipId);
        recommendationCacheService.evictUsers(myUserId, opponentUserId);
    }

    private Friendship getFriendshipOrThrow(Long friendshipId) {
        Friendship friendship = friendMapper.findById(friendshipId);
        if (friendship == null) {
            throw new NotFoundException("요청하신 자원을 찾을 수 없습니다.");
        }
        return friendship;
    }
}
