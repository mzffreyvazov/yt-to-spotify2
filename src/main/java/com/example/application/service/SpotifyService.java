package com.example.application.service;

import java.util.Base64;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.client.RestClient;

import com.example.application.config.SpotifyProperties;
import com.example.application.exception.UpstreamServiceException;
import com.example.application.model.response.SpotifyResponse;
import com.example.application.model.spotify_dto.SpotifySearchApiResponse;
import com.example.application.model.spotify_dto.TrackItem;
import com.example.application.model.spotify_dto.SpotifyAuthResponse;

@Service
public class SpotifyService {

    private final SpotifyProperties spotifyProperties;
    private final RestClient searchWebClient;
    private final RestClient trackWebClient;
    private String accessToken;
    private long tokenExpirationTime;

    // api parameters
    private static final String typeUrlParam = "track";
    private static final int limitUrlParam = 10;
    private static final int offsetUrlParam = 0;

    public SpotifyService(@Qualifier("searchWebClient") RestClient searchWebClient,
                          @Qualifier("trackWebClient") RestClient trackWebClient,
                          SpotifyProperties spotifyProperties) {
        this.searchWebClient = searchWebClient;
        this.trackWebClient = trackWebClient;
        this.spotifyProperties = spotifyProperties;
    }

    private String getCachedAccessToken() {
        // Refresh token if needed (Spotify tokens typically last 1 hour)
        long currentTime = System.currentTimeMillis();
        if (accessToken == null || currentTime >= tokenExpirationTime) {
            accessToken = getAccessToken();
            tokenExpirationTime = currentTime + (3600 * 1000); // 1 hour
        }
        return accessToken;
    }

    private String getAccessToken() {
        String authUrl = spotifyProperties.getAuthUrl();
        String clientId = spotifyProperties.getClientId();
        String clientSecret = spotifyProperties.getClientSecret();

        // For debugging, you can add a log here to check the values:
        System.out.println("SPOTIFY_AUTH_URL: " + authUrl);
        System.out.println("SPOTIFY_CLIENT_ID: " + clientId);
        System.out.println("SPOTIFY_CLIENT_SECRET: " + clientSecret);

        if (clientId == null || clientSecret == null || authUrl == null) {
            throw new IllegalStateException("Spotify client credentials or auth URL not configured.");
        }

        String authHeader = "Basic " + Base64.getEncoder().encodeToString(
            (clientId + ":" + clientSecret).getBytes()
        );

        try {
            SpotifyAuthResponse response = RestClient.builder().build()
                    .post()
                    .uri(authUrl)
                    .header("Authorization", authHeader)
                    .header("Content-Type", "application/x-www-form-urlencoded")
                    .body("grant_type=client_credentials")
                    .retrieve()
                    .body(SpotifyAuthResponse.class);
            return response.getAccessToken();
        } catch (Exception e) {
            System.err.println("Error getting Spotify access token: " + e.getMessage());
            throw new UpstreamServiceException("Failed to obtain Spotify access token", e);
        }
    }


    public List<SpotifyResponse> getSpotifyResponse(String searchQuery) {
        System.out.println("\n[Spotify Search] Query: " + searchQuery);
        String token = getCachedAccessToken();
        
        SpotifySearchApiResponse apiResponse = searchWebClient.get()
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
                .body(SpotifySearchApiResponse.class);
        
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


    public SpotifyResponse getSingleTrack(@RequestParam String trackId) {
        String token = getCachedAccessToken();
        
        TrackItem trackItem = trackWebClient.get()
                .uri("/{id}", trackId)
                .header("Authorization", "Bearer " + token)
                .retrieve()
                .body(TrackItem.class);
        
        if (trackItem == null) {
            throw new UpstreamServiceException("Track not found");
        }
        
        return mapTrackItemToSpotifyResponse(trackItem);
    }
}
