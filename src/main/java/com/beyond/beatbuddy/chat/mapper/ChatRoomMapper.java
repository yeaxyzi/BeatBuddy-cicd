package com.beyond.beatbuddy.chat.mapper;

import com.beyond.beatbuddy.chat.dto.response.ChatMessageResponse;
import com.beyond.beatbuddy.chat.dto.response.ChatRoomListResponse;
import com.beyond.beatbuddy.chat.dto.response.ChatRoomResponse;
import com.beyond.beatbuddy.chat.entity.ChatRoom;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Mapper
public interface ChatRoomMapper {

    // 채팅방 조회
    Optional<ChatRoom> findRoomByUsers(@Param("loginUserId") Long loginUserId,
                                       @Param("opponentUserId") Long opponentUserId);

    // 채팅방 생성
    void insertChatRoom(ChatRoom chatRoom);

    // 멤버 생성
    void insertChatRoomMember(@Param("roomId") Long roomId, @Param("userId") Long userId);

    // 채팅방 재활성화
    void reactivateMember(@Param("roomId") Long roomId, @Param("loginUserId") Long loginUserId);

    // 채팅방 나가기
    void exitChatRoom(@Param("roomId") Long roomId, @Param("loginUserId") Long loginUserId);

    // 채팅방 정보 반환
    ChatRoomResponse findChatRoomInfo(@Param("roomId") Long roomId,
                                      @Param("loginUserId") Long loginUserId);

    // 채팅방 존재 여부
    int existsById(@Param("roomId") Long roomId);

    // 상대방 나갔는지 확인
    boolean isOpponentExited(@Param("roomId") Long roomId, @Param("loginUserId") Long loginUserId);

    // 내가 나갔는지 확인
    boolean isMyExited(@Param("roomId") Long roomId, @Param("loginUserId") Long loginUserId);

    // 상대방 userId 반환
    Long findOpponentUserId(@Param("roomId") Long roomId, @Param("loginUserId") Long loginUserId);

    // rejoined_at 조회
    LocalDateTime findRejoinedAt(@Param("roomId") Long roomId, @Param("loginUserId") Long loginUserId);

    // 메시지 조회
    List<ChatMessageResponse> findMessages(@Param("roomId") Long roomId,
                                           @Param("loginUserId") Long loginUserId,
                                           @Param("opponentUserId") Long opponentUserId,
                                           @Param("rejoinedAt") LocalDateTime rejoinedAt);

    // 읽음 처리
    void updateReadStatus(@Param("roomId") Long roomId, @Param("loginUserId") Long loginUserId);

    // 채팅방 목록 조회
    List<ChatRoomListResponse> findAllByUserId(@Param("loginUserId") Long loginUserId);

    // 데드락 방지
    Long lockChatRoom(@Param("roomId") Long roomId);
}