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
public class Group {

    private Long groupId;

    private String groupName;

    @Builder.Default
    private Integer memberCount = 0;

    private Long creatorId;

    private String description;

    private String groupImageUrl;

    private String inviteCode;

    private LocalDateTime createdAt;

    public void addMember() {
        this.memberCount++;
    }

    public void removeMember() {
        if (this.memberCount > 0) {
            this.memberCount--;
        }
    }
}