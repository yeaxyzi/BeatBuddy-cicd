package com.beyond.beatbuddy.music.dto.response;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Data;
import lombok.Getter;

@Getter
@Data
@JsonIgnoreProperties(ignoreUnknown = true)  // 필요없는 필드 무시용 annotation
public class TrackAnalysisResponse {

	private String Id;
	private String error;

	private Integer popularity;
	private Integer energy;
	private Integer danceability;
	private Integer happiness;
	private Integer acousticness;
	private Integer instrumentalness;
	private Integer liveness;
	private Integer speechiness;

}
