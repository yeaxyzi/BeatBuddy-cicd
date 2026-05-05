package com.beyond.beatbuddy.group.controller;

import com.beyond.beatbuddy.global.dto.ApiResponse;
import com.beyond.beatbuddy.global.util.FileStorageService;
import com.beyond.beatbuddy.global.util.JwtUtil;
import com.beyond.beatbuddy.group.dto.request.GroupCreateRequest;
import com.beyond.beatbuddy.group.dto.request.GroupJoinRequest;
import com.beyond.beatbuddy.group.dto.response.GroupIdResponse;
import com.beyond.beatbuddy.group.dto.response.GroupResponse;
import com.beyond.beatbuddy.group.service.GroupService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@Tag(name = "Group APIs", description = "그룹 관련 API 목록")
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/groups")
@Validated
public class GroupController {

    private final GroupService groupService;
    private final JwtUtil jwtUtil;
    private final FileStorageService fileStorageService;

    @Operation(summary = "그룹명 중복 확인")
    @GetMapping("/name-check")
    public ResponseEntity<ApiResponse<Boolean>> checkGroupName(
            @RequestParam
            @NotBlank(message = "그룹명은 필수입니다.")
            @Size(max = 20, message = "그룹명은 최대 20자입니다.") String groupName) {
        boolean isDuplicate = groupService.checkGroupNameDuplicate(groupName);

        return ApiResponse.of(HttpStatus.OK, "중복 여부 확인 성공", isDuplicate);
    }

    @Operation(summary = "초대코드 중복 확인")
    @GetMapping("/invite-code-check")
    public ResponseEntity<ApiResponse<Boolean>> checkInviteCode(
            @RequestParam
            @NotBlank(message = "초대코드는 필수입니다.") String inviteCode) {
        boolean isDuplicate = groupService.checkInviteCodeDuplicate(inviteCode);

        return ApiResponse.of(HttpStatus.OK, "중복 여부 확인 성공", isDuplicate);
    }

    @Operation(summary = "그룹 생성")
    @io.swagger.v3.oas.annotations.parameters.RequestBody(
            content = @Content(mediaType = "multipart/form-data")
    )
    @PostMapping(consumes = "multipart/form-data")
    public ResponseEntity<ApiResponse<GroupIdResponse>> createGroup(
            @RequestPart("data") @Valid GroupCreateRequest request,
            @RequestPart(value = "groupImage", required = false) MultipartFile groupImage,
            @Parameter(hidden = true) @RequestHeader("Authorization") String token) {

        Long creatorId = jwtUtil.getUserId(jwtUtil.substringToken(token));

        groupService.validateCreateGroup(request);

        String groupImageUrl = fileStorageService.saveGroupImage(groupImage);

        Long groupId = groupService.createGroup(request, creatorId, groupImageUrl);

        return ApiResponse.of(HttpStatus.CREATED, "그룹이 생성되었습니다.", new GroupIdResponse(groupId));
    }

    @Operation(summary = "초대코드로 그룹 조회")
    @GetMapping("/invite/{inviteCode}")
    public ResponseEntity<ApiResponse<GroupResponse>> getGroupByInviteCode(
            @PathVariable
            @NotBlank(message = "초대코드는 필수입니다.") String inviteCode) {
        GroupResponse response = groupService.getGroupByInviteCode(inviteCode.toUpperCase());

        return ApiResponse.of(HttpStatus.OK, "그룹 조회 성공", response);
    }

    @Operation(summary = "그룹 내 닉네임 중복 확인")
    @GetMapping("/{groupId}/nickname-check")
    public ResponseEntity<ApiResponse<Boolean>> checkNickname(
            @PathVariable Long groupId,
            @RequestParam
            @NotBlank(message = "닉네임은 필수입니다.")
            @Size(max = 20, message = "닉네임은 최대 20자입니다.") String nickname) {
        boolean isDuplicate = groupService.isNicknameDuplicate(groupId, nickname);

        return ApiResponse.of(HttpStatus.OK, "중복 여부 확인 성공", isDuplicate);
    }

    @Operation(summary = "그룹 가입")
    @PostMapping("/{groupId}/members")
    public ResponseEntity<ApiResponse<GroupIdResponse>> joinGroup(
            @PathVariable Long groupId,
            @Valid @RequestBody GroupJoinRequest request,
            @Parameter(hidden = true) @RequestHeader("Authorization") String token) {

        Long userId = jwtUtil.getUserId(jwtUtil.substringToken(token));

        Long joinedGroupId = groupService.joinGroup(groupId, request, userId);

        return ApiResponse.of(HttpStatus.CREATED, "그룹 가입이 완료되었습니다.", new GroupIdResponse(joinedGroupId));
    }

    @Operation(summary = "가입 그룹 조회")
    @GetMapping("/my-groups")
    public ResponseEntity<ApiResponse<List<GroupResponse>>> getMyGroups(
            @Parameter(hidden = true) @RequestHeader("Authorization") String token) {

        Long userId = jwtUtil.getUserId(jwtUtil.substringToken(token));
        List<GroupResponse> groups = groupService.getMyGroups(userId);

        return ApiResponse.of(HttpStatus.OK, "가입 그룹 조회 성공", groups);
    }

    @Operation(summary = "그룹 나가기")
    @DeleteMapping("/{groupId}/members/me")
    public ResponseEntity<ApiResponse<Void>> leaveGroup(
            @PathVariable Long groupId,
            @Parameter(hidden = true) @RequestHeader ("Authorization") String token) {

        Long userId = jwtUtil.getUserId(jwtUtil.substringToken(token));
        groupService.leaveGroup(groupId, userId);

        return ApiResponse.of(HttpStatus.OK, "그룹 나가기 성공", null);
    }
}