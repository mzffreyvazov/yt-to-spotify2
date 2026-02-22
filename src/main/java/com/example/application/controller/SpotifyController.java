package com.example.application.controller;

import java.util.List;
import java.util.Map;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.client.RestClient;

import com.example.application.config.ApiProperties;
import com.example.application.model.response.SpotifyResponse;
import com.example.application.service.SpotifyService;
import com.example.application.service.SpotifyUserTokenService;

import jakarta.servlet.http.HttpSession;

@RestController
@RequestMapping("/api/spotify")
public class SpotifyController {

    private final SpotifyService spotifyService;
    private final SpotifyUserTokenService userTokenService;
    private final RestClient spotifyClient;

    public SpotifyController(SpotifyService spotifyService,
                             SpotifyUserTokenService userTokenService,
                             ApiProperties apiProperties) {
        this.spotifyService   = spotifyService;
        this.userTokenService = userTokenService;
        this.spotifyClient    = RestClient.builder()
                .baseUrl(apiProperties.getSpotify().getBaseUrl())
                .build();
    }

    @GetMapping("/search")
    public List<SpotifyResponse> getMethodName(@RequestParam String query) {
        return spotifyService.getSpotifyResponse(query);
    }

    @GetMapping("/search/track")
    public SpotifyResponse getTrack(@RequestParam String trackId) {
        return spotifyService.getSingleTrack(trackId);
    }

    /**
     * Saves a track to the current user's Liked Songs playlist.
     * Requires the user to be authenticated via the Authorization Code Flow
     * (see /api/spotify/auth/login).
     */
    @PostMapping("/me/tracks")
    public ResponseEntity<Map<String, Object>> saveTrack(@RequestParam String trackId,
                                                          HttpSession session) {
        if (!userTokenService.isLoggedIn(session)) {
            return ResponseEntity.status(401)
                    .body(Map.of("error", "NOT_AUTHENTICATED",
                                 "message", "Please log in with Spotify first."));
        }

        String userToken = userTokenService.getUserAccessToken(session);

        // Spotify expects a JSON array of IDs: PUT /v1/me/tracks?ids=trackId
        spotifyClient.put()
                .uri(uriBuilder -> uriBuilder.path("/v1/me/tracks")
                        .queryParam("ids", trackId)
                        .build())
                .header("Authorization", "Bearer " + userToken)
                .header("Content-Type", "application/json")
                .retrieve()
                .toBodilessEntity();

        return ResponseEntity.ok(Map.of("saved", true, "trackId", trackId));
    }
}

