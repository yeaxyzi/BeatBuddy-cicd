package com.beyond.beatbuddy.chat.mapper;

import com.beyond.beatbuddy.chat.dto.response.ChatMessageResponse;
import com.beyond.beatbuddy.chat.entity.ChatMessage;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Options;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Result;
import org.apache.ibatis.annotations.Results;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;

import java.util.List;

@Mapper
public interface ChatMessageMapper {

    // 메시지 저장
    void insertMessage(ChatMessage chatMessage);

    // chat_rooms last_message 업데이트
    void updateLastMessage(@Param("messageId") Long messageId,
                           @Param("messageText") String messageText,
                           @Param("roomId") Long roomId);

    // 상대방 unread_count 증가
    void incrementUnreadCount(@Param("roomId") Long roomId,
                              @Param("senderId") Long senderId);

    // receiverId 추출
    Long findReceiverIdByRoomId(@Param("roomId") Long roomId,
                                @Param("senderId") Long senderId);

    // 전송한 메시지 반환
    ChatMessageResponse findById(@Param("messageId") Long messageId);

    void lockChatRoom(Long roomId);
}