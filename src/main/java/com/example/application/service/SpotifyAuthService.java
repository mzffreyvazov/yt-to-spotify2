package com.example.application.service;

import java.util.Base64;

import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;

import com.example.application.config.SpotifyProperties;
import com.example.application.exception.UpstreamServiceException;
import com.example.application.model.spotify_dto.SpotifyAuthResponse;

@Service
public class SpotifyAuthService {

    private final SpotifyProperties spotifyProperties;
    private final RestClient authClient;

    private String accessToken;
    private long tokenExpirationTime;

    public SpotifyAuthService(SpotifyProperties spotifyProperties) {
        this.spotifyProperties = spotifyProperties;
        // Auth client has no base URL — the full auth URL comes from properties
        this.authClient = RestClient.builder().build();
    }

    public String getCachedAccessToken() {
        long currentTime = System.currentTimeMillis();
        if (accessToken == null || currentTime >= tokenExpirationTime) {
            accessToken = fetchNewToken();
            tokenExpirationTime = currentTime + (3600 * 1000);
        }
        return accessToken;
    }

    private String fetchNewToken() {
        String clientId = spotifyProperties.getClientId();
        String clientSecret = spotifyProperties.getClientSecret();
        String authUrl = spotifyProperties.getAuthUrl();

        if (clientId == null || clientSecret == null || authUrl == null) {
            throw new IllegalStateException("Spotify credentials not configured.");
        }

        String authHeader = "Basic " + Base64.getEncoder()
                .encodeToString((clientId + ":" + clientSecret).getBytes());

        try {
            SpotifyAuthResponse response = authClient.post()
                    .uri(authUrl)
                    .header("Authorization", authHeader)
                    .header("Content-Type", "application/x-www-form-urlencoded")
                    .body("grant_type=client_credentials")
                    .retrieve()
                    .body(SpotifyAuthResponse.class);

            if (response == null || response.getAccessToken() == null) {
                throw new UpstreamServiceException("Spotify auth response or access token is null");
            }
            return response.getAccessToken();
        } catch (Exception e) {
            throw new UpstreamServiceException("Failed to obtain Spotify access token", e);
        }
    }
}
