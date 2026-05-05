package com.beyond.beatbuddy.global.entity;

import lombok.*;
import java.time.LocalDateTime;

@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class User {
    private Long userId;
    private String email;
    private String password;
    private String kakaoId;
    private String provider;        // LOCAL, KAKAO

    private String nickname;
    private String gender;          // MALE, FEMALE
    private Integer birthYear;
    private String profileImageUrl;

    private Boolean isTutorialViewed;
    private Boolean isTasteAnalyzed;

    private String status;          // ACTIVE, DELETED

    private Boolean allowPushChat;
    private Boolean allowPushSocial;

    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
