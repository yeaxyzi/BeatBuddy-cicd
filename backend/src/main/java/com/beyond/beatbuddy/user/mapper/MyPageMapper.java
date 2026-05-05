package com.beyond.beatbuddy.user.mapper;

import com.beyond.beatbuddy.global.entity.User;
import com.beyond.beatbuddy.user.dto.response.UserGroupNicknameItemResponse;
import com.beyond.beatbuddy.user.dto.response.UserNotificationSettingResponse;
import java.util.List;
import java.util.Optional;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

@Mapper
public interface MyPageMapper {
    Optional<User> selectUserByEmail(@Param("email") String email);

    UserNotificationSettingResponse selectNotificationSetting(@Param("userId") Long userId);

    void updateChatNotificationSetting(@Param("userId") Long userId,
                                       @Param("allowPushChat") Boolean allowPushChat);

    void updateSocialNotificationSetting(@Param("userId") Long userId,
                                         @Param("allowPushSocial") Boolean allowPushSocial);

    List<UserGroupNicknameItemResponse> selectMyGroupNicknames(@Param("userId") Long userId);

    int countDuplicateGroupNickname(@Param("groupId") Long groupId,
                                    @Param("userId") Long userId,
                                    @Param("groupNickname") String groupNickname);

    int updateGroupNickname(@Param("userId") Long userId,
                            @Param("groupId") Long groupId,
                            @Param("groupNickname") String groupNickname);

    void updatePassword(@Param("userId") Long userId, @Param("newPassword") String newPassword);

    void updateProfileImage(@Param("userId") Long userId, @Param("profileImageUrl") String profileImageUrl);

    void withdrawUser(@Param("userId") Long userId);
}
