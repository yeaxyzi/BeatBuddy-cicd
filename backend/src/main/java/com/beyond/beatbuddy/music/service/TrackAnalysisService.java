package com.beyond.beatbuddy.music.service;

import com.beyond.beatbuddy.global.exception.BusinessException;
import com.beyond.beatbuddy.music.dto.response.TrackAnalysisResponse;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import com.google.common.util.concurrent.RateLimiter;

import java.io.IOException;
import java.net.URI;
import java.net.URLEncoder;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;

@Service
public class TrackAnalysisService {

	@Value("${rapidapi.key}")
	private String trackAnalysisKey;

	@Value("${rapidapi.track-analysis-host}")
	private String trackAnalysisHost;

	private final ObjectMapper objectMapper = new ObjectMapper();
	private final HttpClient client = HttpClient.newHttpClient();
	private final RateLimiter rateLimiter = RateLimiter.create(8.0);

	public TrackAnalysisResponse getFeatures(String spotifyId, String trackName, String artistName) {
		rateLimiter.acquire();
		return requestBySpotifyId(spotifyId);
	}

	// spotifyId 방식
	private TrackAnalysisResponse requestBySpotifyId(String spotifyId) {

		String fullUrl = "https://track-analysis.p.rapidapi.com/pktx/spotify/" + spotifyId;
		return sendRequest(fullUrl);
	}

	// 곡명 + 가수명 방식
	private TrackAnalysisResponse requestBySongAndArtist(String trackName, String artistName) {
		String encodedSong = URLEncoder.encode(trackName, StandardCharsets.UTF_8);
		String encodedArtist = URLEncoder.encode(artistName, StandardCharsets.UTF_8);

		String fullUrl = "https://track-analysis.p.rapidapi.com/pktx/rapid?song="
				+ encodedSong + "&artist=" + encodedArtist;

		return sendRequest(fullUrl);
	}

	// 공통 요청 메서드
	private TrackAnalysisResponse sendRequest(String fullUrl) {
		HttpRequest request = HttpRequest.newBuilder()
				.uri(URI.create(fullUrl))
				.header("x-rapidapi-key", trackAnalysisKey)
				.header("x-rapidapi-host", trackAnalysisHost)
				.GET()
				.build();

		try {
			HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

			// 실패 시 response body도 같이 보여주기
			if (response.statusCode() != 200) {
				throw new BusinessException(
						HttpStatus.INTERNAL_SERVER_ERROR,
						"RapidAPI 호출 실패: " + response.statusCode() + ", body=" + response.body()
				);
			}

			TrackAnalysisResponse parsed =
					objectMapper.readValue(response.body(), TrackAnalysisResponse.class);

			if (parsed.getError() != null && !parsed.getError().isBlank()) {
				throw new BusinessException(
						HttpStatus.INTERNAL_SERVER_ERROR,
						"RapidAPI 분석 실패"
				);
			}

			if (parsed.getId() == null) {
				throw new BusinessException(
						HttpStatus.INTERNAL_SERVER_ERROR,
						"유효하지 않은 곡"
				);
			}

			if (parsed.getPopularity() == null
					|| parsed.getEnergy() == null
					|| parsed.getDanceability() == null
					|| parsed.getHappiness() == null
					|| parsed.getAcousticness() == null
					|| parsed.getInstrumentalness() == null
					|| parsed.getLiveness() == null
					|| parsed.getSpeechiness() == null) {

				throw new BusinessException(
						HttpStatus.INTERNAL_SERVER_ERROR,
						"분석값 없음"
				);
			}

			return parsed;

		} catch (InterruptedException e) {
			Thread.currentThread().interrupt();
			throw new BusinessException(HttpStatus.INTERNAL_SERVER_ERROR, "RapidAPI 호출이 중단되었습니다.");
		} catch (IOException e) {
			throw new BusinessException(HttpStatus.INTERNAL_SERVER_ERROR, "RapidAPI 호출 실패: " + e.getMessage());
		}
	}
}