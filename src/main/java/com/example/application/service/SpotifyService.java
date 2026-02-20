package com.example.application.service;

import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;

import com.example.application.exception.UpstreamServiceException;
import com.example.application.model.response.SpotifyResponse;
import com.example.application.model.spotify_dto.SpotifySearchApiResponse;
import com.example.application.model.spotify_dto.TrackItem;

@Service
public class SpotifyService {

    private final RestClient spotifyClient;
    private final SpotifyAuthService spotifyAuthService;

    private static final String SEARCH_PATH = "/v1/search";
    private static final String TRACK_PATH  = "/v1/tracks";
    private static final String TYPE_PARAM  = "track";
    private static final int    LIMIT_PARAM = 10;
    private static final int    OFFSET_PARAM = 0;

    public SpotifyService(@Qualifier("spotifyClient") RestClient spotifyClient,
                          SpotifyAuthService spotifyAuthService) {
        this.spotifyClient = spotifyClient;
        this.spotifyAuthService = spotifyAuthService;
    }

    public List<SpotifyResponse> getSpotifyResponse(String searchQuery) {
        String token = spotifyAuthService.getCachedAccessToken();

        SpotifySearchApiResponse apiResponse = spotifyClient.get()
                .uri(uriBuilder -> uriBuilder
                        .path(SEARCH_PATH)
                        .queryParam("q", searchQuery)
                        .queryParam("type", TYPE_PARAM)
                        .queryParam("limit", LIMIT_PARAM)
                        .queryParam("offset", OFFSET_PARAM)
                        .queryParam("market", "US")
                        .build())
                .header("Authorization", "Bearer " + token)
                .retrieve()
                .body(SpotifySearchApiResponse.class);

        if (apiResponse == null
                || apiResponse.getTracks() == null
                || apiResponse.getTracks().getItems() == null) {
            return Collections.emptyList();
        }

        return apiResponse.getTracks().getItems().stream()
                .map(this::mapTrackItemToSpotifyResponse)
                .filter(Objects::nonNull)
                .collect(Collectors.toList());
    }

    public SpotifyResponse getSingleTrack(String trackId) {
        String token = spotifyAuthService.getCachedAccessToken();

        TrackItem trackItem = spotifyClient.get()
                .uri(TRACK_PATH + "/{id}", trackId)
                .header("Authorization", "Bearer " + token)
                .retrieve()
                .body(TrackItem.class);

        if (trackItem == null) {
            throw new UpstreamServiceException("Track not found");
        }

        return mapTrackItemToSpotifyResponse(trackItem);
    }

    private SpotifyResponse mapTrackItemToSpotifyResponse(TrackItem item) {
        if (item == null
                || item.getAlbum() == null
                || item.getAlbum().getImages() == null
                || item.getArtists() == null
                || item.getArtists().isEmpty()) {
            return null;
        }

        String albumImageUrl = item.getAlbum().getImages().stream()
                .max(Comparator.comparingInt(img -> img.getWidth()))
                .map(img -> img.getUrl())
                .orElse(null);

        return new SpotifyResponse(
                item.getId(),
                item.getName(),
                item.getArtists().get(0).getName(),
                albumImageUrl,
                item.getPreview_url(),
                item.getExternalUrls() != null ? item.getExternalUrls().getSpotify() : null
        );
    }
}
