package com.beyond.beatbuddy.music.service;


import com.beyond.beatbuddy.music.dto.response.MusicSearchResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import se.michaelthelin.spotify.SpotifyApi;
import se.michaelthelin.spotify.model_objects.credentials.ClientCredentials;
import se.michaelthelin.spotify.model_objects.specification.AlbumSimplified;
import se.michaelthelin.spotify.model_objects.specification.ArtistSimplified;
import se.michaelthelin.spotify.model_objects.specification.Paging;
import se.michaelthelin.spotify.model_objects.specification.Track;
import se.michaelthelin.spotify.requests.authorization.client_credentials.ClientCredentialsRequest;
import se.michaelthelin.spotify.requests.data.search.simplified.SearchTracksRequest;

import java.util.ArrayList;
import java.util.List;

// 라이브러리로 토큰 발급 + 검색을 처리

@Service
@RequiredArgsConstructor
public class SpotifyService {

    private final SpotifyApi spotifyApi;

    /*
    * access token 발급
    *  - setAccessTOken()
    *     : Spotify API를 쓰기 전에 토큰을 발급받아 spotifyApi 객체에 넣음
    */
    private void setAccessToken() {
        try {
            ClientCredentialsRequest request = spotifyApi.clientCredentials().build();
            ClientCredentials credentials = request.execute();

            spotifyApi.setAccessToken(credentials.getAccessToken());

        } catch (Exception e) {
            throw new RuntimeException("Spotify 토큰 발급 실패", e);
        }
    }

    /*
    * 음악 검색
    *  - searchMusic(String keyword)
    *     : 검색어를 받아서 Spotify에서 곡 찾고 MusicSearchResponse 리스트로 바꿔줌
    */
    public List<MusicSearchResponse> searchMusic(String keyword) {

        setAccessToken();  // 토큰 세팅

        try {
            SearchTracksRequest request = spotifyApi.searchTracks(keyword)
                    .limit(10)
                    .build();

            Paging<Track> trackPaging = request.execute();
            Track[] tracks = trackPaging.getItems();

            List<MusicSearchResponse> result = new ArrayList<>();

            for (Track track : tracks) {

                AlbumSimplified album = track.getAlbum();
                ArtistSimplified[] artists = track.getArtists();

                String artistName = (artists.length > 0) ? artists[0].getName() : null;
                String albumImage = (album.getImages() != null && album.getImages().length > 0)
                        ? album.getImages()[0].getUrl()
                        : null;

                result.add(MusicSearchResponse.builder()
                        .trackId(track.getId())
                        .trackName(track.getName())
                        .artistName(artistName)
                        .albumId(album.getId())
                        .albumName(album.getName())
                        .albumCoverUrl(albumImage)
                        .build());
            }
            return result;
        } catch (Exception e) {
            throw new RuntimeException("Spotify 검색 실패", e);
        }
    }

    /*
    * 곡 상세 조회
    *  - trackId로 Spotify에서 곡 1개의 상세 정보를 조회
    *  - 저장 API에서 사용
    */
    public MusicSearchResponse getTrackInfo(String trackId) {

        setAccessToken();

        try {
            Track track = spotifyApi.getTrack(trackId)
                    .build()
                    .execute();

            AlbumSimplified album = track.getAlbum();
            ArtistSimplified[] artists = track.getArtists();

            String artistName = (artists.length > 0) ? artists[0].getName() : null;
            String albumImage = (album.getImages() != null && album.getImages().length > 0)
                    ? album.getImages()[0].getUrl()
                    : null;

            return MusicSearchResponse.builder()
                    .trackId(track.getId())
                    .trackName(track.getName())
                    .artistName(artistName)
                    .albumId(album.getId())
                    .albumName(album.getName())
                    .albumCoverUrl(albumImage)
                    .build();

        } catch (Exception e) {
            throw new RuntimeException("Spotify 곡 상세 조회 실패", e);
        }
    }
}
