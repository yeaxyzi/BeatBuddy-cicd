package com.beyond.beatbuddy.chat.service;

import com.beyond.beatbuddy.chat.dto.response.ChatMessageResponse;
import com.beyond.beatbuddy.chat.dto.response.ChatRoomEnterResponse;
import com.beyond.beatbuddy.chat.dto.response.ChatRoomListResponse;
import com.beyond.beatbuddy.chat.dto.response.ChatRoomResponse;
import com.beyond.beatbuddy.chat.entity.ChatRoom;
import com.beyond.beatbuddy.chat.mapper.ChatMessageMapper;
import com.beyond.beatbuddy.chat.mapper.ChatRoomMapper;
import com.beyond.beatbuddy.global.dto.ApiResponse;
import com.beyond.beatbuddy.global.exception.BadRequestException;
import com.beyond.beatbuddy.global.exception.ForbiddenException;
import com.beyond.beatbuddy.global.exception.NotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Slf4j
@Service
@Transactional
@RequiredArgsConstructor
public class ChatRoomService {
    private final ChatRoomMapper chatRoomMapper;
    private final ChatMessageMapper chatMessageMapper;

    // 채팅방 조회 or 생성
    public ResponseEntity<ApiResponse<ChatRoomResponse>> createChatRoom(Long loginUserId, Long opponentUserId, Long groupId) {
        if (loginUserId.equals(opponentUserId)) {
            throw new BadRequestException("자기 자신에게 채팅을 보낼 수 없습니다.");
        }

        Optional<ChatRoom> existingRoom = chatRoomMapper.findRoomByUsers(loginUserId, opponentUserId);

        if (existingRoom.isPresent()) {
            Long roomId = existingRoom.get().getRoomId();
            if (chatRoomMapper.isMyExited(roomId, loginUserId)) {
                chatRoomMapper.reactivateMember(roomId, loginUserId);
                // 상대방도 나갔으면 재활성화
                if (chatRoomMapper.isMyExited(roomId, opponentUserId)) {
                    chatRoomMapper.reactivateMember(roomId, opponentUserId);
                }
            }
            return ApiResponse.of(HttpStatus.OK, "채팅방 조회 성공",
                    chatRoomMapper.findChatRoomInfo(roomId, loginUserId));
        }

        // 새 채팅방 생성
        ChatRoom newRoom = new ChatRoom();
        newRoom.setGroupId(groupId);
        newRoom.setUserAId(Math.min(loginUserId, opponentUserId));
        newRoom.setUserBId(Math.max(loginUserId, opponentUserId));
        chatRoomMapper.insertChatRoom(newRoom);
        chatRoomMapper.insertChatRoomMember(newRoom.getRoomId(), loginUserId);
        chatRoomMapper.insertChatRoomMember(newRoom.getRoomId(), opponentUserId);

        return ApiResponse.of(HttpStatus.CREATED, "채팅방이 생성되었습니다.",
                chatRoomMapper.findChatRoomInfo(newRoom.getRoomId(), loginUserId));
    }

    // 채팅방 입장
    @Transactional
    public ChatRoomEnterResponse enterChatRoom(Long roomId, Long loginUserId) {
        if (chatRoomMapper.existsById(roomId) == 0) {
            throw new NotFoundException("채팅방을 찾을 수 없습니다.");
        }

        Long opponentUserId = chatRoomMapper.findOpponentUserId(roomId, loginUserId);
        if (opponentUserId == null) {
            throw new ForbiddenException("채팅방에 접근할 수 없습니다.");
        }

        // rejoined_at 조회
        LocalDateTime rejoinedAt = chatRoomMapper.findRejoinedAt(roomId, loginUserId);

        // 메시지 조회
        List<ChatMessageResponse> messages = chatRoomMapper.findMessages(
                roomId, loginUserId, opponentUserId, rejoinedAt);

        // 읽음 처리
        chatRoomMapper.updateReadStatus(roomId, loginUserId);

        // 상대방 나갔는지 확인
        boolean isOpponentExited = chatRoomMapper.isOpponentExited(roomId, loginUserId);

        return new ChatRoomEnterResponse(messages, isOpponentExited);

    }

    // 채팅방 목록 조회
    @Transactional(readOnly = true)
    public List<ChatRoomListResponse> getChatRooms(Long loginUserId) {
        return chatRoomMapper.findAllByUserId(loginUserId);
    }

    // 채팅방 나가기
    @Transactional
    public void exitChatRoom(Long roomId, Long loginUserId) {
        if (chatRoomMapper.existsById(roomId) == 0) {
            throw new NotFoundException("채팅방을 찾을 수 없습니다.");
        }
        Long opponentUserId = chatRoomMapper.findOpponentUserId(roomId, loginUserId);
        if (opponentUserId == null) {
            throw new ForbiddenException("채팅방에 접근할 수 없습니다.");
        }
        chatRoomMapper.exitChatRoom(roomId, loginUserId);
    }

    // 채팅방에서 읽기
    @Transactional
    public void markAsRead(Long roomId, Long loginUserId) {
        chatRoomMapper.lockChatRoom(roomId);
        chatRoomMapper.updateReadStatus(roomId, loginUserId);
    }
}

