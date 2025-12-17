package com.example.application.service;

import java.util.Base64;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.reactive.function.client.WebClient;

import com.example.application.model.response.SpotifyResponse;
import com.example.application.model.spotify_dto.SpotifySearchApiResponse;
import com.example.application.model.spotify_dto.TrackItem;
import com.example.application.model.spotify_dto.SpotifyAuthResponse;
import reactor.core.publisher.Mono;

@Service
public class SpotifyService {

    @Value("${SPOTIFY_CLIENT_SECRET}")
    private String SPOTIFY_CLIENT_SECRET;

    @Value("${SPOTIFY_CLIENT_ID}")
    private String SPOTIFY_CLIENT_ID;
    
    @Value("${SPOTIFY_AUTH_URL}")
    private String SPOTIFY_AUTH_URL;


    private WebClient searchWebClient;
    private WebClient trackWebClient;
    private Mono<String> accessTokenMono;

    // api parameters
    private static final String typeUrlParam = "track";
    private static final int limitUrlParam = 10;
    private static final int offsetUrlParam = 0;

    public SpotifyService(@Qualifier("searchWebClient") WebClient searchWebClient,
                          @Qualifier("trackWebClient") WebClient trackWebClient) {
        this.searchWebClient = searchWebClient;
        this.trackWebClient = trackWebClient;
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
        System.out.println("\n[Spotify Search] Query: " + searchQuery);
        return getCachedAccessToken().flatMap(token ->
            searchWebClient.get()
                    .uri(uriBuilder -> {
                        // Use queryParam for standard parameters
                        uriBuilder.queryParam("q", searchQuery)
                                .queryParam("type", typeUrlParam)
                                .queryParam("limit", limitUrlParam)
                                .queryParam("offset", offsetUrlParam)
                                .queryParam("market", "US");
                                // .queryParam("include_external", "audio");
                        
                        // Append locale as raw query string to preserve semicolons and encoded values
                        String currentQuery = uriBuilder.build().getQuery();
                        String newQuery = currentQuery + "&locale=en-US,en;q%3D0.9,tr;q%3D0.8";
                        
                        return uriBuilder.replaceQuery(newQuery).build();
                    })
                    .header("Authorization", "Bearer " + token)
                    .retrieve()
                    .bodyToMono(SpotifySearchApiResponse.class)
                    .map(apiResponse -> {
                        if (apiResponse == null || apiResponse.getTracks() == null || apiResponse.getTracks().getItems() == null) {
                            System.out.println("[Spotify Search] No results found");
                            return Collections.emptyList(); // Return an empty list if no data
                        }
                        
                        // Log the tracks.href parameter
                        System.out.println("[Spotify Search] API Response HREF: " + apiResponse.getTracks().getHref());
                        
                        // Map the API response to a list of SpotifyResponse objects
                        List<SpotifyResponse> results = apiResponse.getTracks().getItems().stream()
                                .map(this::mapTrackItemToSpotifyResponse)
                                .filter(item -> item != null) // Filter out any null responses
                                .collect(Collectors.toList());
                        
                        System.out.println("[Spotify Search] Found " + results.size() + " tracks:");
                        for (int i = 0; i < results.size() && i < 10; i++) {
                            SpotifyResponse track = results.get(i);
                            System.out.println("  " + (i + 1) + ". " + track.getSongTitle() + " - " + track.getArtistName());
                            System.out.println("     URL: " + track.getSpotifyUrl());
                        }
                        return results;
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

    // private SpotifyResponse mapTrackItemToSpotifyResponse(SingleTrackItem item) {
    //     if (item == null || item.getAlbum() == null || item.getAlbum().getReleaseDate() == null || item.getArtist() == null) {
    //         return null;
    //     }

    //     String trackId = item.getId();
    //     String songTitle = item.getName();
    //     String artistName = item.getArtist().getName();
    //     String spotifyUrl = (item.getExternalUrls() != null) ? item.getExternalUrls().getSpotify() : null;
    //     String previewUrl = item.getPreviewUrl();

    //     String albumImageUrl = null;
    //     if (!item.getAlbum().getImages().isEmpty()) {
    //         // Sort by width descending to get the largest image
    //         item.getAlbum().getImages().sort((img1, img2) -> Integer.compare(img2.getWidth(), img1.getWidth()));
    //         albumImageUrl = item.getAlbum().getImages().get(0).getUrl();
    //     }

    //     // Add the missing return statement
    //     return new SpotifyResponse(trackId, songTitle, artistName, albumImageUrl, previewUrl, spotifyUrl);
    // }


    public Mono<SpotifyResponse> getSingleTrack(@RequestParam String trackId) {
        return getCachedAccessToken().flatMap(token ->
            trackWebClient.get()
                    .uri("/{id}", trackId)
                    .header("Authorization", "Bearer " + token)
                    .retrieve()
                    .bodyToMono(TrackItem.class)  // Changed from SingleSpotifySearchApiResponse.class to TrackItem.class
                    .map(this::mapTrackItemToSpotifyResponse)  // Use the first mapper method directly
                    .switchIfEmpty(Mono.error(new RuntimeException("Track not found")))
        );
    }
}
