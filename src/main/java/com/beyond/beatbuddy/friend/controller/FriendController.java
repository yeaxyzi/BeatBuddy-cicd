package com.beyond.beatbuddy.friend.controller;

import com.beyond.beatbuddy.friend.dto.FriendDetailResponse;
import com.beyond.beatbuddy.friend.dto.FriendRequest;
import com.beyond.beatbuddy.friend.dto.FriendResponse;
import com.beyond.beatbuddy.friend.service.FriendService;
import com.beyond.beatbuddy.global.dto.ApiResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import com.beyond.beatbuddy.global.security.UserPrincipal;
import jakarta.validation.Valid;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Tag(name = "친구", description = "친구 요청·수락·거절·목록·상세·삭제 API")
@RestController
@RequestMapping("/api/v1/friends")
@RequiredArgsConstructor
public class FriendController {

    private final FriendService friendService;

    /** FRIEND_001 - 친구 요청 보내기 */
    @Operation(summary = "친구 요청 보내기")
    @PostMapping("/requests")
    public ResponseEntity<ApiResponse<Void>> sendRequest(
            @AuthenticationPrincipal UserPrincipal userPrincipal,
            @Valid @RequestBody FriendRequest dto) {
        Long myUserId = userPrincipal.getUserId();
        friendService.sendFriendRequest(myUserId, dto);
        return ApiResponse.of(HttpStatus.CREATED, "친구 요청을 성공적으로 보냈습니다.", null);
    }

    /** FRIEND_002 - 친구 요청 수락 */
    @Operation(summary = "친구 요청 수락")
    @PostMapping("/requests/{requestId}/accept")
    public ResponseEntity<ApiResponse<Void>> acceptRequest(
            @AuthenticationPrincipal UserPrincipal userPrincipal,
            @PathVariable Long requestId) {
        Long myUserId = userPrincipal.getUserId();
        friendService.acceptFriendRequest(myUserId, requestId);
        return ApiResponse.of(HttpStatus.OK, "친구 요청을 수락했습니다.", null);
    }

    /** FRIEND_003 - 친구 요청 거절 */
    @Operation(summary = "친구 요청 거절")
    @PostMapping("/requests/{requestId}/reject")
    public ResponseEntity<ApiResponse<Void>> rejectRequest(
            @AuthenticationPrincipal UserPrincipal userPrincipal,
            @PathVariable Long requestId) {
        Long myUserId = userPrincipal.getUserId();
        friendService.rejectFriendRequest(myUserId, requestId);
        return ApiResponse.of(HttpStatus.OK, "친구 요청을 거절했습니다.", null);
    }

    /** FRIEND_004 - 내 친구 목록 조회 */
    @Operation(summary = "내 친구 목록 조회")
    @GetMapping
    public ResponseEntity<ApiResponse<List<FriendResponse>>> getMyFriends(
            @AuthenticationPrincipal UserPrincipal userPrincipal) {
        Long myUserId = userPrincipal.getUserId();
        List<FriendResponse> friends = friendService.getMyFriends(myUserId);
        return ApiResponse.of(HttpStatus.OK, "친구 목록 조회 성공", friends);
    }

    /** FRIEND_005 - 친구 상세 정보 조회 */
    @Operation(summary = "친구 상세 정보 조회")
    @GetMapping("/{friendId}")
    public ResponseEntity<ApiResponse<FriendDetailResponse>> getFriendDetail(
            @AuthenticationPrincipal UserPrincipal userPrincipal,
            @PathVariable Long friendId) {
        Long myUserId = userPrincipal.getUserId();
        FriendDetailResponse detail = friendService.getFriendDetail(myUserId, friendId);
        return ApiResponse.of(HttpStatus.OK, "친구 상세 정보 조회 성공", detail);
    }

    /** FRIEND_006 - 친구 삭제 */
    @Operation(summary = "친구 삭제")
    @DeleteMapping("/{friendId}")
    public ResponseEntity<ApiResponse<Void>> deleteFriend(
            @AuthenticationPrincipal UserPrincipal userPrincipal,
            @PathVariable Long friendId) {
        Long myUserId = userPrincipal.getUserId();
        friendService.deleteFriend(myUserId, friendId);
        return ApiResponse.of(HttpStatus.OK, "친구 관계를 성공적으로 삭제했습니다.", null);
    }

    /** FRIEND_007 - 내가 받은 친구 요청 목록 조회 */
    @Operation(summary = "받은 친구 요청 목록 조회")
    @GetMapping("/requests/received")
    public ResponseEntity<ApiResponse<List<FriendResponse>>> getReceivedRequests(
            @AuthenticationPrincipal UserPrincipal userPrincipal) {
        Long myUserId = userPrincipal.getUserId();
        List<FriendResponse> requests = friendService.getReceivedRequests(myUserId);
        return ApiResponse.of(HttpStatus.OK, "받은 친구 요청 목록 조회 성공", requests);
    }
}
