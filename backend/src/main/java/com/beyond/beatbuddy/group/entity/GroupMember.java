package com.beyond.beatbuddy.group.entity;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class GroupMember {

    private Long memberId;

    private Long groupId;

    private Long userId;

    private String groupNickname;

    private LocalDateTime joinedAt;
}