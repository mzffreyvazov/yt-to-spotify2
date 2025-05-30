package com.example.application.service;

import java.util.Base64;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;

import com.example.application.model.response.SpotifyResponse;
import com.example.application.model.spotify_dto.SpotifySearchApiResponse;
import com.example.application.model.spotify_dto.TrackItem;
import com.example.application.model.spotify_dto.SpotifyAuthResponse;
import reactor.core.publisher.Mono;

@Service
public class SpotifyService {

    // base url: https://api.spotify.com/v1/search
    @Value("${SPOTIFY_CLIENT_SECRET}")
    private String SPOTIFY_CLIENT_SECRET;

    @Value("${SPOTIFY_CLIENT_ID}")
    private String SPOTIFY_CLIENT_ID;
    
    @Value("${SPOTIFY_AUTH_URL}")
    private String SPOTIFY_AUTH_URL;


    private WebClient webClient;
    private Mono<String> accessTokenMono;

    // api parameters
    private static final String typeUrlParam = "track";
    private static final int limitUrlParam = 10;
    private static final int offsetUrlParam = 0;



    public SpotifyService(WebClient.Builder webClientBuilder) {
        this.webClient = webClientBuilder.baseUrl("https://api.spotify.com/v1/search").build();
    }

    private Mono<String> getCachedAccessToken() {
        // Refresh token if needed (Spotify tokens typically last 1 hour)
        if (accessTokenMono == null) {
            accessTokenMono = getAccessToken().cache();
        }
        return accessTokenMono;
    }

    private Mono<String> getAccessToken() {
        // For debugging, you can add a log here to check the values:
        System.out.println("SPOTIFY_AUTH_URL: " + SPOTIFY_AUTH_URL);
        System.out.println("SPOTIFY_CLIENT_ID: " + SPOTIFY_CLIENT_ID);
        System.out.println("SPOTIFY_CLIENT_SECRET: " + SPOTIFY_CLIENT_SECRET);

        if (SPOTIFY_CLIENT_ID == null || SPOTIFY_CLIENT_SECRET == null || SPOTIFY_AUTH_URL == null) {
            return Mono.error(new IllegalStateException("Spotify client credentials or auth URL not configured."));
        }

        String authHeader = "Basic " + Base64.getEncoder().encodeToString(
            (SPOTIFY_CLIENT_ID + ":" + SPOTIFY_CLIENT_SECRET).getBytes()
        );

        return WebClient.builder().build() 
                .post()
                .uri(SPOTIFY_AUTH_URL) 
                .header("Authorization", authHeader)
                .header("Content-Type", "application/x-www-form-urlencoded")
                .bodyValue("grant_type=client_credentials")
                .retrieve()
                .bodyToMono(SpotifyAuthResponse.class)
                .map(SpotifyAuthResponse::getAccessToken)
                .doOnError(e -> System.err.println("Error getting Spotify access token: " + e.getMessage()))
                .onErrorResume(e -> Mono.error(new RuntimeException("Failed to obtain Spotify access token", e)));
    }


    public Mono<List<SpotifyResponse>> getSpotifyResponse(String searchQuery) {
        return getCachedAccessToken().flatMap(token ->
            webClient.get()
                    .uri(uriBuilder -> uriBuilder
                            .queryParam("q", searchQuery)
                            .queryParam("type", typeUrlParam)
                            .queryParam("limit", limitUrlParam)
                            .queryParam("offset", offsetUrlParam)
                            .build())
                    .header("Authorization", "Bearer " + token)
                    .retrieve()
                    .bodyToMono(SpotifySearchApiResponse.class)
                    .map(apiResponse -> {
                        if (apiResponse == null || apiResponse.getTracks() == null || apiResponse.getTracks().getItems() == null) {
                            return Collections.emptyList(); // Return an empty list if no data
                        }
                        // Map the API response to a list of SpotifyResponse objects
                        return apiResponse.getTracks().getItems().stream()
                                .map(this::mapTrackItemToSpotifyResponse)
                                .filter(item -> item != null) // Filter out any null responses
                                .collect(Collectors.toList());
                    })
        );
    }

    private SpotifyResponse mapTrackItemToSpotifyResponse(TrackItem item) {
        if (item == null || item.getAlbum() == null || item.getAlbum().getImages() == null || item.getArtists() == null || item.getArtists().isEmpty()) {
            return null;
        }

        String trackId = item.getId();
        String songTitle = item.getName();
        String artistName = item.getArtists().get(0).getName();
        String previewUrl = item.getPreview_url();
        String spotifyUrl = (item.getExternalUrls() != null) ? item.getExternalUrls().getSpotify() : null;

        String albumImageUrl = null;
        if (!item.getAlbum().getImages().isEmpty()) {
            // Sort by width descending to get the largest image
            item.getAlbum().getImages().sort((img1, img2) -> Integer.compare(img2.getWidth(), img1.getWidth()));
            albumImageUrl = item.getAlbum().getImages().get(0).getUrl();
        }

        return new SpotifyResponse(trackId, songTitle, artistName, albumImageUrl, previewUrl, spotifyUrl);
    }

}
