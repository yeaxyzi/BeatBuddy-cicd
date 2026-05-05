package com.beyond.beatbuddy.chat.controller;

import com.beyond.beatbuddy.chat.dto.request.ChatMessageRequest;
import com.beyond.beatbuddy.chat.dto.response.ChatMessageResponse;
import com.beyond.beatbuddy.chat.dto.response.EventResponse;
import com.beyond.beatbuddy.chat.entity.ChatMessage;
import com.beyond.beatbuddy.chat.mapper.ChatMessageMapper;
import com.beyond.beatbuddy.chat.mapper.ChatRoomMapper;
import com.beyond.beatbuddy.chat.service.ChatMessageService;
import com.beyond.beatbuddy.chat.service.ChatRoomService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.ibatis.annotations.Param;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.simp.SimpMessageHeaderAccessor;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@Slf4j
@Controller
@RequiredArgsConstructor
public class ChatMessageController {
    private final ChatMessageService chatMessageService;
    private final SimpMessagingTemplate messagingTemplate;
    private final ChatMessageMapper chatMessageMapper;

    private final ChatRoomService chatRoomService;
    private final ChatRoomMapper chatRoomMapper;

    @MessageMapping("/chat/message")
    public void sendMessage(ChatMessageRequest request, SimpMessageHeaderAccessor accessor) {

        Long senderId = (Long) accessor.getSessionAttributes().get("userId");

        // 메시지 저장
        ChatMessageResponse chatMessage = chatMessageService.sendMessage(request.getRoomId(), senderId, request.getMessageText());

        // 구독자한테 전송
        messagingTemplate.convertAndSend("/sub/chat/rooms/" + request.getRoomId(), chatMessage);

        // 수신자한테 NEW_MESSAGE 이벤트 푸시
        Long receiverId = chatMessageMapper.findReceiverIdByRoomId(request.getRoomId(), senderId);

        log.info("이벤트 푸시: /sub/events/{}", receiverId);
        messagingTemplate.convertAndSend("/sub/events/" + receiverId, new EventResponse("NEW_MESSAGE", request.getRoomId()));
    }

    @MessageMapping("/chat/read")
    public void markAsRead(Map<String, Long> payload, SimpMessageHeaderAccessor accessor) {
        Long loginUserId = (Long) accessor.getSessionAttributes().get("userId");
        Long roomId = payload.get("roomId");

        chatRoomService.markAsRead(roomId, loginUserId);

        Long opponentUserId = chatRoomMapper.findOpponentUserId(roomId, loginUserId);
        messagingTemplate.convertAndSend("/sub/events/" + opponentUserId,
                new EventResponse("MESSAGE_READ", roomId));

        log.info("읽음처리: roomId={}, loginUserId={}, opponentUserId={}", roomId, loginUserId, opponentUserId);
    }
}
