package com.beyond.beatbuddy.music.dto.request;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Getter;

import java.util.List;

@Getter
public class SaveTasteRequest {
	@NotNull(message = "트랙 목록을 입력해주세요")
	@Size(min = 10, max = 10, message = "트랙은 정확히 10개여야 합니다")
	private List<TrackInfo> tracks;

	@Getter
	public static class TrackInfo {
		private String trackId;
		private String trackName;
		private String artistName;
		private String albumId;
		private String albumName;
		private String coverUrl;
	}
}