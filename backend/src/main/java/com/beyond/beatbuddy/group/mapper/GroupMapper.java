package com.beyond.beatbuddy.group.mapper;

import com.beyond.beatbuddy.group.entity.Group;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;
import java.util.Optional;

@Mapper
public interface GroupMapper {

    boolean existsByGroupName(String groupName);

    boolean existsByInviteCode(String inviteCode);

    Optional<Group> findByInviteCode(String inviteCode);

    Optional<Group> findById(Long groupId);

    boolean existsById(Long groupId);

    void save(Group group);

    void updateMemberCount(@Param("groupId") Long groupId, @Param("memberCount") int memberCount);

    List<Group> findGroupsByUserId(Long userId);

    void deleteById(Long groupId);
}