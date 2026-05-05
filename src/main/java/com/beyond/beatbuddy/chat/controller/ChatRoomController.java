package com.beyond.beatbuddy.chat.controller;

import com.beyond.beatbuddy.chat.dto.request.CreateChatRoomRequest;
import com.beyond.beatbuddy.chat.dto.response.ChatRoomEnterResponse;
import com.beyond.beatbuddy.chat.dto.response.ChatRoomListResponse;
import com.beyond.beatbuddy.chat.dto.response.ChatRoomResponse;
import com.beyond.beatbuddy.chat.dto.response.EventResponse;
import com.beyond.beatbuddy.chat.mapper.ChatRoomMapper;
import com.beyond.beatbuddy.chat.service.ChatRoomService;
import com.beyond.beatbuddy.global.dto.ApiResponse;
import com.beyond.beatbuddy.global.util.AuthUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@Slf4j
@RestController
@RequestMapping("/api/v1/chat")
@RequiredArgsConstructor
public class ChatRoomController {
    private final ChatRoomService chatRoomService;
    private final ChatRoomMapper chatRoomMapper;
    private final SimpMessagingTemplate messagingTemplate;


    // 채팅룸 생성
    @PostMapping("/rooms")
    public ResponseEntity<ApiResponse<ChatRoomResponse>> createChatRoom(
            @RequestBody CreateChatRoomRequest request) {
        Long loginUserId = AuthUtil.getCurrentUserId();
        return chatRoomService.createChatRoom(loginUserId, request.getOpponentUserId(), request.getGroupId());
    }

    //.채팅방 입장
    @GetMapping("/{roomId}/messages")
    public ResponseEntity<ApiResponse<ChatRoomEnterResponse>> enterChatRoom(
            @PathVariable Long roomId) {
        Long loginUserId = AuthUtil.getCurrentUserId();

        // 발신자한테 MESSAGE_READ 이벤트 푸시
        Long opponentUserId = chatRoomMapper.findOpponentUserId(roomId, loginUserId);
        if (opponentUserId != null) {
            messagingTemplate.convertAndSend("/sub/events/" + opponentUserId,
                    new EventResponse("MESSAGE_READ", roomId));
        }

        return ApiResponse.of(HttpStatus.OK, "조회 성공", chatRoomService.enterChatRoom(roomId, loginUserId));
    }

    // 채팅방 목록 조회
    @GetMapping("/rooms")
    public ResponseEntity<ApiResponse<List<ChatRoomListResponse>>> getChatRooms() {
        Long loginUserId = AuthUtil.getCurrentUserId();
        return ApiResponse.of(HttpStatus.OK, "채팅방 목록 조회 성공", chatRoomService.getChatRooms(loginUserId));
    }

    // 채팅방 나가기
    @PatchMapping("/{roomId}/exit")
    public ResponseEntity<ApiResponse<Object>> exitChatRoom(@PathVariable Long roomId) {
        Long loginUserId = AuthUtil.getCurrentUserId();

        // 상대방한테 OPPONENT_EXITED 이벤트 푸시
        Long opponentUserId = chatRoomMapper.findOpponentUserId(roomId, loginUserId);
        if (opponentUserId != null) {
            messagingTemplate.convertAndSend("/sub/events/" + opponentUserId,
                    new EventResponse("OPPONENT_EXITED", roomId));
        }

        chatRoomService.exitChatRoom(roomId, loginUserId);
        return ApiResponse.of(HttpStatus.OK, "채팅방에서 나갔습니다.", null);
    }
}