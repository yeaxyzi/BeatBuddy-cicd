package com.beyond.beatbuddy.group.service;

import com.beyond.beatbuddy.auth.mapper.UserMapper;
import com.beyond.beatbuddy.global.entity.User;
import com.beyond.beatbuddy.global.exception.BadRequestException;
import com.beyond.beatbuddy.global.exception.ConflictException;
import com.beyond.beatbuddy.global.exception.NotFoundException;
import com.beyond.beatbuddy.group.dto.request.GroupCreateRequest;
import com.beyond.beatbuddy.group.dto.request.GroupJoinRequest;
import com.beyond.beatbuddy.group.dto.response.GroupResponse;
import com.beyond.beatbuddy.group.entity.Group;
import com.beyond.beatbuddy.group.entity.GroupMember;
import com.beyond.beatbuddy.group.mapper.GroupMapper;
import com.beyond.beatbuddy.group.mapper.GroupMemberMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class GroupService {

    private final GroupMapper groupMapper;
    private final GroupMemberMapper groupMemberMapper;
    private final UserMapper userMapper;

    public boolean checkGroupNameDuplicate(String groupName) {
        return groupMapper.existsByGroupName(groupName);
    }

    public boolean checkInviteCodeDuplicate(String inviteCode) {
        return groupMapper.existsByInviteCode(inviteCode);
    }

    public void validateCreateGroup(GroupCreateRequest request) {
        if (groupMapper.existsByGroupName(request.getGroupName())) {

            throw new ConflictException("이미 존재하는 그룹명입니다.");
        }

        String invitecode = request.getInviteCode();

        if (groupMapper.existsByInviteCode(invitecode)) {
            throw new ConflictException("이미 사용 중인 초대 코드입니다");
        }
    }

    @Transactional
    public Long createGroup(GroupCreateRequest request, Long creatorId, String groupImageUrl) {

        String inviteCode = request.getInviteCode();
        String groupNickname = resolveGroupNickname(request.getGroupNickname(), creatorId);

        Group group = Group.builder()
                .groupName(request.getGroupName())
                .description(request.getDescription())
                .inviteCode(inviteCode)
                .groupImageUrl(groupImageUrl)
                .creatorId(creatorId)
                .memberCount(0)
                .build();

        groupMapper.save(group);
        groupMapper.updateMemberCount(group.getGroupId(), 1);

        GroupMember firstMember = GroupMember.builder()
                .groupId(group.getGroupId())
                .userId(creatorId)
                .groupNickname(groupNickname)
                .build();

        groupMemberMapper.save(firstMember);

        return group.getGroupId();
    }

    public GroupResponse getGroupByInviteCode(String inviteCode) {
        Group group = groupMapper.findByInviteCode(inviteCode)
                .orElseThrow(() -> new NotFoundException("존재하지 않는 초대코드입니다."));

        return GroupResponse.builder()
                .groupId(group.getGroupId())
                .groupName(group.getGroupName())
                .description(group.getDescription())
                .groupImageUrl(group.getGroupImageUrl())
                .memberCount(group.getMemberCount())
                .build();
    }

    public boolean isNicknameDuplicate(Long groupId, String nickname) {
        if (!groupMapper.existsById(groupId)) {
            throw new NotFoundException("존재하지 않는 그룹입니다.");
        }
        return groupMemberMapper.existsByGroupIdAndGroupNickname(groupId, nickname);
    }

    @Transactional
    public Long joinGroup(Long groupId, GroupJoinRequest request, Long userId) {

        Group group = groupMapper.findById(groupId)
                .orElseThrow(() -> new NotFoundException("존재하지 않는 그룹입니다."));
        String groupNickname = resolveGroupNickname(request.getGroupNickname(), userId);

        if (!group.getInviteCode().equals(request.getInviteCode())) {
            throw new BadRequestException("초대코드가 올바르지 않습니다.");
        }

        if (groupMemberMapper.existsByGroupIdAndUserId(groupId, userId)) {
            throw new ConflictException("이미 가입한 그룹입니다.");
        }

        if (groupMemberMapper.existsByGroupIdAndGroupNickname(groupId, groupNickname)) {
            throw new ConflictException("이미 사용 중인 닉네임입니다.");
        }

        GroupMember member = GroupMember.builder()
                .groupId(groupId)
                .userId(userId)
                .groupNickname(groupNickname)
                .build();

        groupMemberMapper.save(member);

        groupMapper.updateMemberCount(groupId, group.getMemberCount() + 1);

        return groupId;
    }

    public List<GroupResponse> getMyGroups(Long userId) {
        List<Group> groups = groupMapper.findGroupsByUserId(userId);

        return groups.stream()
                .map(group -> GroupResponse.builder()
                        .groupId(group.getGroupId())
                        .groupName(group.getGroupName())
                        .description(group.getDescription())
                        .memberCount(group.getMemberCount())
                        .groupImageUrl(group.getGroupImageUrl())
                        .inviteCode(group.getInviteCode())
                        .build())
                .collect(Collectors.toList());
    }

    @Transactional
    public void leaveGroup(Long groupId, Long userId) {

        Group group = groupMapper.findById(groupId)
                .orElseThrow(() -> new NotFoundException("존재하지 않는 그룹입니다."));

        if (!groupMemberMapper.existsByGroupIdAndUserId(groupId, userId)) {
            throw new NotFoundException("가입되지 않은 그룹입니다.");
        }

        groupMemberMapper.deleteByGroupIdAndUserId(groupId, userId);

        int updatedCount = group.getMemberCount() - 1;

        groupMapper.updateMemberCount(groupId, updatedCount);

        if (updatedCount == 0) {
            groupMapper.deleteById(groupId);
        }
    }

    private String resolveGroupNickname(String requestedGroupNickname, Long userId) {
        if (requestedGroupNickname != null && !requestedGroupNickname.isBlank()) {
            return requestedGroupNickname.trim();
        }

        User user = userMapper.findByUserId(userId);
        if (user == null) {
            throw new NotFoundException("사용자를 찾을 수 없습니다.");
        }
        return user.getNickname();
    }
}
