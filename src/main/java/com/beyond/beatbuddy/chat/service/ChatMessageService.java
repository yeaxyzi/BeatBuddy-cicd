package com.beyond.beatbuddy.chat.service;

import com.beyond.beatbuddy.chat.dto.response.ChatMessageResponse;
import com.beyond.beatbuddy.chat.entity.ChatMessage;
import com.beyond.beatbuddy.chat.mapper.ChatMessageMapper;
import lombok.RequiredArgsConstructor;
import org.apache.ibatis.annotations.Param;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
@RequiredArgsConstructor
public class ChatMessageService {

    private final ChatMessageMapper chatMessageMapper;

    public ChatMessageResponse sendMessage(Long roomId, Long senderId, String messageText) {

        chatMessageMapper.lockChatRoom(roomId);
        // 1. 메시지 저장
        ChatMessage chatMessage = new ChatMessage();
        chatMessage.setRoomId(roomId);
        chatMessage.setSenderId(senderId);
        chatMessage.setMessageText(messageText);
        chatMessageMapper.insertMessage(chatMessage);

        // 2. 마지막 메시지 업데이트
        chatMessageMapper.updateLastMessage(chatMessage.getMessageId(), messageText, roomId);

        // 3. 상대방 unread_count 증가
        chatMessageMapper.incrementUnreadCount(roomId, senderId);

        // 4. 저장된 메시지 반환
        ChatMessageResponse response = chatMessageMapper.findById(chatMessage.getMessageId());
        response.setIsRead(false);
        return response;
    }
}
