package com.beyond.beatbuddy.music.controller;

import com.beyond.beatbuddy.global.dto.ApiResponse;
import com.beyond.beatbuddy.music.dto.request.SaveTasteRequest;
import com.beyond.beatbuddy.music.dto.response.TrackSearchResponse;
import com.beyond.beatbuddy.music.dto.response.TasteResponse;
import com.beyond.beatbuddy.music.service.MusicService;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Validated
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/music")
public class MusicController {

	private final MusicService musicService;

	// 사용자가 본인의 최애 음악을 서치할 때 호출
	@GetMapping("/search")
	public ResponseEntity<ApiResponse<List<TrackSearchResponse>>> searchTracks(
			@RequestParam @NotBlank(message = "검색어를 입력해주세요") String keyword) {
		List<TrackSearchResponse> response = musicService.searchTracks(keyword);
		return ApiResponse.of(HttpStatus.OK, "음악 검색이 완료됐습니다.", response);
	}

	// 사용자가 본인의 최애 음악 10개를 고르고 나서 호출
	@PostMapping("/taste")
	public ResponseEntity<ApiResponse<Void>> saveTaste(
			@RequestBody @Valid SaveTasteRequest request) {
		musicService.saveTaste(request);
		return ApiResponse.of(HttpStatus.OK, "취향이 저장됐습니다.", null);
	}

    /*
     * 저장된 취향 조회
     *  - 음악 탭 진입 시 저장된 취향 데이터가 있는지 확인하고 변환
     */
    @GetMapping("/taste")
    public ResponseEntity<ApiResponse<TasteResponse>> getTaste() {

        TasteResponse result = musicService.getTaste();

		String message;

		if (Boolean.FALSE.equals(result.getIsTasteAnalyzed())
				&& (result.getTracks() == null || result.getTracks().isEmpty())) {
			message = "저장된 취향 데이터가 없습니다.";
		} else {
			message = "취향 조회에 성공했습니다.";
		}

        return ApiResponse.of(HttpStatus.OK, message, result);
    }

    /*
     * 취향 수정
     *  - 기존 10곡을 전체 교체
     */
    @PutMapping("/taste")
    public ResponseEntity<ApiResponse<Void>> updateTaste(
            @RequestBody @Valid SaveTasteRequest request) {

        musicService.updateTaste(request);
        return ApiResponse.of(HttpStatus.OK, "취향이 성공적으로 수정되었습니다.", null);
    }

    // config에서 열어놨음 로그인 안해도 접속 가능하도록,,,
	// 스포티파이 곡 아이디로 곡 특성 가져오기
	@GetMapping("/justfortest")
	public ResponseEntity<ApiResponse<Object>> searchTest (
			@RequestParam String spotifyId) {
		Object data = musicService.searchTest(spotifyId);
		return ApiResponse.of(HttpStatus.OK, "테스트 - 음악 특성 가져오기 성공", data);
	}

}
