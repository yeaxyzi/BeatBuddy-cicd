package com.beyond.beatbuddy.group.mapper;

import com.beyond.beatbuddy.group.entity.GroupMember;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface GroupMemberMapper {

    boolean existsByGroupIdAndUserId(@Param("groupId") Long groupId, @Param("userId") Long userId);

    boolean existsByGroupIdAndGroupNickname(@Param("groupId") Long groupId, @Param("groupNickname") String groupNickname);

    void save(GroupMember groupMember);

    void deleteByGroupIdAndUserId(@Param("groupId") Long groupId, @Param("userId") Long userId);
}