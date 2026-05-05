package com.beyond.beatbuddy.music.service;

import com.beyond.beatbuddy.auth.mapper.UserMapper;
import com.beyond.beatbuddy.music.dto.response.TasteResponse;
import com.beyond.beatbuddy.global.exception.BadRequestException;
import com.beyond.beatbuddy.global.exception.BusinessException;
import com.beyond.beatbuddy.global.util.AuthUtil;
import com.beyond.beatbuddy.music.dto.request.SaveTasteRequest;
import com.beyond.beatbuddy.music.dto.response.TrackAnalysisResponse;
import com.beyond.beatbuddy.music.dto.response.TrackSearchResponse;
import com.beyond.beatbuddy.music.mapper.MusicMapper;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.jdbc.UncategorizedSQLException;
import se.michaelthelin.spotify.SpotifyApi;
import se.michaelthelin.spotify.exceptions.SpotifyWebApiException;
import se.michaelthelin.spotify.model_objects.specification.Paging;
import se.michaelthelin.spotify.model_objects.specification.Track;
import se.michaelthelin.spotify.requests.authorization.client_credentials.ClientCredentialsRequest;

import java.io.IOException;
import org.apache.hc.core5.http.ParseException;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;
import java.util.HashSet;
import java.util.Set;

@Service
@RequiredArgsConstructor
public class MusicService {
	private static final double[] TASTE_VECTOR_WEIGHTS = {
			0.2, 0.1, // popularity mean/std
			1.3, 0.8, // energy mean/std
			1.5, 0.9, // danceability mean/std
			0.8, 0.5, // happiness mean/std
			1.4, 0.8, // acousticness mean/std
			1.8, 1.0, // instrumentalness mean/std
			0.7, 0.4, // liveness mean/std
			1.8, 1.0  // speechiness mean/std
	};

	private final UserProfileService userProfileService;
	private final MusicMapper musicMapper;                     // DB 쿼리 담당 인터페이스
	private final TrackAnalysisService trackAnalysisService;   // RapidAPI 호출 담당
	private final UserMapper userMapper;

	@Value("${rapidapi.track-analysis-host}")
	private String trackAnalysisHost;                          // application-secret.yml에서 주입

	@Value("${rapidapi.key}")
	private String trackAnalysisKey;                           // application-secret.yml에서 주입

	@Value("${spotify.client-id}")
	private String clientId;                                   // application-secret.yml에서 주입

	@Value("${spotify.client-secret}")
	private String clientSecret;                               // application-secret.yml에서 주입

	// Spotify API 호출 시 필요한 액세스 토큰 발급하는 거 - 안 보셔도 됨
	private String getAccessToken() {
		try {
			// 위에 있는 거
			SpotifyApi spotifyApi = new SpotifyApi.Builder()
					.setClientId(clientId)
					.setClientSecret(clientSecret)
					.build();
			// 토큰 발급 요청 객체 생성
			ClientCredentialsRequest request = spotifyApi
					.clientCredentials()
					.build();
			// 실제 토큰 발급 후 문자열로 반환
			return request.execute().getAccessToken();

		} catch (IOException | ParseException e) {
			// 네트워크 오류, 응답 파싱 실패
			throw new BusinessException(HttpStatus.INTERNAL_SERVER_ERROR, "Spotify 토큰 발급 실패: " + e.getMessage());
		} catch (SpotifyWebApiException e) {
			// Spotify 서버 측 오류 (인증 실패, 서버 장애 등)
			throw new BusinessException(HttpStatus.INTERNAL_SERVER_ERROR, "Spotify API 오류: " + e.getMessage());
		}
	}

	public List<TrackSearchResponse> searchTracks(String keyword) {
		if (keyword == null || keyword.isBlank()) {
			// @Validated + @NotBlank로 컨트롤러에서 이미 검증하지만 그냥 한 번 더 했음
			throw new BadRequestException("검색어를 입력해주세요");
		}

		try {
			// 매 검색마다 새 토큰 발급 후 API 클라이언트 생성
			// 토큰 캐싱 최적화도 할 수 있는데,,,, 뭐 힘들 거 같고
			SpotifyApi spotifyApi = new SpotifyApi.Builder()
					.setAccessToken(getAccessToken())
					.build();

			// Spotify에서 키워드로 트랙 검색, 최대 10개 반환
			Paging<Track> result = spotifyApi.searchTracks(keyword)
					.limit(10)
					.build()
					.execute();

			// Track 객체 배열 → TrackSearchResponse DTO 리스트로 변환

			// List<TrackSearchResponse> list = new ArrayList<>();
			// for (Track track : result.getItems()) {
			//    list.add(new TrackSearchResponse(...));
			//  }  ===> 이렇게 해도 됨
			return Arrays.stream(result.getItems())                    // Track[] 배열 => Stream<Track>으로 변환
					.map(track -> new TrackSearchResponse(       // 각 Track => TrackSearchResponse로 변환
							track.getId(),                             // Spotify 트랙 ID
							track.getName(),                           // 곡명
							track.getArtists()[0].getName(),           // 그냥 가장 첫 번째 아티스트명
							track.getAlbum().getId(),                  // 앨범 ID (DB PK용)
							track.getAlbum().getName(),                // 앨범명
							track.getAlbum().getImages()[1].getUrl()   // 앨범 커버 이미지 (중간 사이즈?? 괜찮지 않을까)
					))
					.collect(Collectors.toList());                     // Stream<TrackSearchResponse> => List<TrackSearchResponse>로 변환
			// stream()으로 열었으면 마지막에 collect()로 닫아줘야 함
		} catch (IOException | ParseException e) {
			throw new BusinessException(HttpStatus.INTERNAL_SERVER_ERROR, "Spotify 검색 실패: " + e.getMessage());
		} catch (SpotifyWebApiException e) {
			throw new BusinessException(HttpStatus.INTERNAL_SERVER_ERROR, "Spotify API 오류: " + e.getMessage()); // 여기
		}
	}

	// @Transactional: 메서드 전체가 하나의 트랜잭션 - DB 수업에서 배운 거라 보시면 됨, JAVA 수업에서도 했고
	// 중간에 예외 발생 시 앞에서 성공한 INSERT들도 전부 롤백
	@Transactional
	public void saveTaste(SaveTasteRequest request) {
		// JWT에서 현재 로그인한 사용자 ID 추출
		Long userId = AuthUtil.getCurrentUserId();

		// 이미 취향 저장된 사용자인지 확인
		Boolean isTasteAnalyzed = userMapper.findIsTasteAnalyzedByUserId(userId);
		if (Boolean.TRUE.equals(isTasteAnalyzed)) {
			throw new BusinessException(
					HttpStatus.BAD_REQUEST,
					"이미 취향이 저장된 사용자입니다. 수정 기능을 사용해주세요."
			);
		}

		saveTasteInternal(userId, request);

	}

	@Transactional
	public void updateTaste(SaveTasteRequest request) {
		Long userId = AuthUtil.getCurrentUserId();

		musicMapper.deleteUserFavMusic(userId);

		saveTasteInternal(userId, request);
	}

	// track 분석 속도 완화
	private void saveTasteInternal(Long userId, SaveTasteRequest request) {
		// track 목록
		List<SaveTasteRequest.TrackInfo> tracks = request.getTracks();
		validateTrackCount(tracks);  // 10곡 검사
		validateDuplicateTracks(tracks);  // 중복 곡 검사

		// RapidAPI(음악 분석 API)에서 가져온 각 곡의 음악적 특성 저장용 리스트
		// DB 먼저 조회하고 없는 곡만 RapidAPI 병렬 호출
		List<TrackAnalysisResponse> featureList = tracks.stream()
				.map(track -> {

					TrackAnalysisResponse cachedFeatures =
							musicMapper.findFeaturesByTrackId(track.getTrackId());

					if (cachedFeatures != null) {
						// DB에 있으면 RapidAPI 호출 없이 즉시 반환
						System.out.println("DB 캐시 히트 - RapidAPI 호출 스킵: " + track.getTrackId());
						return cachedFeatures;
					}

					// DB에 없을 때만 RapidAPI 호출
					System.out.println("분석 시작 trackId = " + track.getTrackId());

					TrackAnalysisResponse features = trackAnalysisService.getFeatures(
							track.getTrackId(),
							track.getTrackName(),
							track.getArtistName()
					);

					System.out.println("분석 성공 trackId = " + track.getTrackId());
					return features;
				})
				.collect(Collectors.toList());

		// 분석 전부 성공할 시 저장
		for (int i = 0; i < tracks.size(); i++) {

			SaveTasteRequest.TrackInfo track = tracks.get(i);
			TrackAnalysisResponse features = featureList.get(i);

			musicMapper.insertAlbumIgnore(
					track.getAlbumId(),
					track.getAlbumName(),
					track.getCoverUrl()
			);

			// music_features 테이블에 저장, INSERT IGNORE = 이미 있으면 스킵
			// 여러 유저가 같은 곡 선택해도 특성 데이터는 한 번만 저장됨
			musicMapper.insertMusicFeaturesIgnore(track, features);

			// user_fav_music 테이블에 저장: "이 유저가 이 곡을 좋아한다"는 관계 저장
			musicMapper.insertUserFavMusic(userId, track.getTrackId());

		}


		// 10곡의 특성값으로 취향 벡터 계산 (평균 8개 + 표준편차 8개 = 16차원)
		double[] vector = calculateTasteVector(featureList);
		// MariaDB VEC_FromText()가 받을 수 있는 "[0.5, 0.3, ...]" 형식으로 변환
		String tasteVector = Arrays.toString(vector);

		// user_profiles 테이블에 저장
		// upsert = 없으면 INSERT, 있으면 UPDATE
		// musicMapper.upsertUserProfile(userId, tasteVector);

		// user_profiles 저장 시 1020 충돌 1회 재시도
		// saveUserProfileWithRetry(userId, tasteVector);
		userProfileService.saveWithRetry(userId, tasteVector);
		// users 테이블의 is_taste_analyzed = true
		// 취향 탭 진입 시 이 값으로 편집모드 / 프로필모드 분기 : 중요!!
		musicMapper.updateIsTasteAnalyzed(userId);
	}

	// user_profiles 저장 시 1020 에러가 나면 1회 재시도
	private void saveUserProfileWithRetry(Long userId, String tasteVector) {
		try {
			saveUserProfile(userId, tasteVector);
		} catch (UncategorizedSQLException e) {
			if (e.getSQLException() != null && e.getSQLException().getErrorCode() == 1020) {
				System.out.println("1020 충돌 발생 - user_profiles 저장 1회 재시도");
				saveUserProfile(userId, tasteVector);
			} else {
				throw e;
			}
		}
	}

	// user_profiles 저장 실제 로직
	private void saveUserProfile(Long userId, String tasteVector) {
		int inserted = musicMapper.insertUserProfileIgnore(userId, tasteVector);

		if (inserted == 0) {  // INSERT IGNORE -> 이미 존재하면 0 반환
			musicMapper.updateUserProfile(userId, tasteVector);
		}
	}

	private void validateSpotifyTrack(String trackId) {
		try {
			SpotifyApi spotifyApi = new SpotifyApi.Builder()
					.setAccessToken(getAccessToken())
					.build();

			spotifyApi.getTrack(trackId).build().execute();

		} catch (IOException | ParseException e) {
			throw new BusinessException(HttpStatus.INTERNAL_SERVER_ERROR, "Spotify 트랙 검증 실패: " + e.getMessage());
		} catch (SpotifyWebApiException e) {
			throw new BusinessException(HttpStatus.BAD_REQUEST, "유효하지 않은 Spotify 트랙입니다.");
		}
	}

	public Object searchTest(String spotifyId){
		// 1. URL 합치기 (반드시 변수에 다시 담아야 함!)
		String baseUrl = "https://track-analysis.p.rapidapi.com/pktx/spotify/";
		String fullUrl = baseUrl + spotifyId;

		// 2. Client 생성
		HttpClient client = HttpClient.newHttpClient();
		HttpRequest request = HttpRequest.newBuilder()
				.uri(URI.create(fullUrl))
				.header("x-rapidapi-key", trackAnalysisKey)
				.header("x-rapidapi-host", trackAnalysisHost)
				.header("Content-Type", "application/json")
				.GET() // .method("GET", ...) 대신 간단하게 .GET() 가능
				.build();

		try {
			// 3. 실제 전송 및 예외 발생 지점
			HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
			// HTTP 상태 코드가 200(OK)이 아닐 경우 처리 (선택)
			if (response.statusCode() != 200) {
				System.out.println("에러 발생! 상태코드: " + response.statusCode());
			}

			System.out.println(response.body());
			ObjectMapper objectMapper = new ObjectMapper();
			return objectMapper.readValue(response.body(), Object.class);

		} catch (IOException e) {
			// 네트워크 연결 오류, 타임아웃 등
			System.err.println("통신 중 입출력 에러 발생: " + e.getMessage());
			throw new RuntimeException("API 호출 실패", e);
		} catch (InterruptedException e) {
			// 전송 중 스레드가 중단되었을 때
			Thread.currentThread().interrupt(); // 현재 스레드 상태 복구
			throw new RuntimeException("작업이 중단되었습니다.", e);
		}
	}

	private double[] calculateTasteVector(List<TrackAnalysisResponse> featureList) {
		// 8개 feature 추출
		// ex) matrix[0] = 첫 번째 곡의 [popularity, energy, danceability, ...]
		int[][] matrix = featureList.stream()
				.map(f -> new int[]{
						f.getPopularity(), f.getEnergy(), f.getDanceability(), f.getHappiness(),
						f.getAcousticness(), f.getInstrumentalness(), f.getLiveness(), f.getSpeechiness()
				})
				.toArray(int[][]::new); // Stream을 2차원 배열로 변환

		double[] vector = new double[16]; // 평균 8 + 표준편차 8 저장
		int n = featureList.size();

		for (int i = 0; i < 8; i++) {
			// 평균
			double mean = 0;
			for (int j = 0; j < n; j++) mean += matrix[j][i];
			mean /= n;
			vector[i * 2] = mean / 100.0;

			// 표준편차 계산: 각 곡이 평균에서 얼마나 떨어져 있는지
			// 표준편차가 크다 = 취향이 다양하다
			// 표준편차가 작다 = 취향이 일관적이다
			double variance = 0;
			for (int j = 0; j < n; j++) variance += Math.pow(matrix[j][i] - mean, 2);
			vector[i * 2 + 1] = Math.sqrt(variance / n) / 100.0;
		}

		for (int i = 0; i < vector.length; i++) {
			vector[i] *= TASTE_VECTOR_WEIGHTS[i];
		}

		return vector;
	}

	private void validateTrackCount(List<SaveTasteRequest.TrackInfo> tracks) {
		if (tracks == null || tracks.size() != 10) {
			throw new BusinessException(HttpStatus.BAD_REQUEST, "최애곡은 반드시 10곡이어야 합니다.");
		}
	}

	private void validateDuplicateTracks(List<SaveTasteRequest.TrackInfo> tracks) {
		Set<String> trackIds = new HashSet<>();

		for (SaveTasteRequest.TrackInfo track : tracks) {
			if (!trackIds.add(track.getTrackId())) {
				throw new BusinessException(HttpStatus.BAD_REQUEST, "중복된 곡은 저장할 수 없습니다.");
			}
		}
	}

	// 취향 조회
	public TasteResponse getTaste() {
		Long userId = AuthUtil.getCurrentUserId();

		List<TasteResponse.TrackInfo> tracks =
				musicMapper.findTasteTracksByUserId(userId);

		Boolean isTasteAnalyzed = userMapper.findIsTasteAnalyzedByUserId(userId);

		return TasteResponse.builder()
				.isTasteAnalyzed(isTasteAnalyzed)
				.tracks(tracks)
				.build();
	}
}
