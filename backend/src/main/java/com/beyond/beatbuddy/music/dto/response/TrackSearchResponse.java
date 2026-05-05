package com.beyond.beatbuddy.music.dto.response;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class TrackSearchResponse {
	private String trackId;    // 나중에 분석 API에 넣을 것
	private String trackName;
	private String artistName;
	private String albumId;
	private String albumName;
	private String coverUrl;
}
