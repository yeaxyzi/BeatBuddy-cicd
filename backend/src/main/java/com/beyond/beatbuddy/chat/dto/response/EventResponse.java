package com.beyond.beatbuddy.chat.dto.response;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
@AllArgsConstructor
public class EventResponse {
    private String type;   // NEW_MESSAGE, MESSAGE_READ, OPPONENT_EXITED
    private Long roomId;
}
