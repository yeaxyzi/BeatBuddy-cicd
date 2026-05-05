package com.beyond.beatbuddy.group.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class GroupResponse {
    private Long groupId;
    private String groupName;
    private String description;
    private Integer memberCount;
    private String groupImageUrl;
    private String inviteCode;
}