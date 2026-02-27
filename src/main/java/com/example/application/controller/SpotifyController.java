package com.example.application.controller;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientResponseException;

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
        this.spotifyService = spotifyService;
        this.userTokenService = userTokenService;
        this.spotifyClient = RestClient.builder()
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

    @GetMapping("/me/tracks/contains")
    public ResponseEntity<Map<String, Object>> containsTracks(@RequestParam List<String> trackIds,
                                                              HttpSession session) {
        if (!userTokenService.isLoggedIn(session)) {
            return ResponseEntity.status(401)
                    .body(Map.of("error", "NOT_AUTHENTICATED",
                            "message", "Please log in with Spotify first."));
        }

        List<String> normalizedTrackIds = trackIds.stream()
                .filter(trackId -> trackId != null && !trackId.isBlank())
                .toList();
        if (normalizedTrackIds.isEmpty()) {
            return ResponseEntity.ok(Map.of("savedByTrack", Map.of()));
        }

        List<String> normalizedTrackUris = normalizedTrackIds.stream().map(this::toTrackUri).toList();
        String userToken = userTokenService.getUserAccessToken(session);

        try {
            Boolean[] savedStatuses = callContains(userToken, normalizedTrackIds, normalizedTrackUris);
            Map<String, Boolean> savedByTrack = new LinkedHashMap<>();

            for (int index = 0; index < normalizedTrackIds.size(); index++) {
                boolean isSaved = savedStatuses != null
                        && index < savedStatuses.length
                        && Boolean.TRUE.equals(savedStatuses[index]);
                savedByTrack.put(normalizedTrackIds.get(index), isSaved);
            }

            return ResponseEntity.ok(Map.of("savedByTrack", savedByTrack));
        } catch (RestClientResponseException ex) {
            if (ex.getStatusCode().value() == 401) {
                userTokenService.clearTokens(session);
                return ResponseEntity.status(401)
                        .body(Map.of("error", "NOT_AUTHENTICATED",
                                "message", "Spotify session expired. Please log in again."));
            }
            if (ex.getStatusCode().value() == 403) {
                return ResponseEntity.ok(Map.of("savedByTrack", Map.of()));
            }
            throw ex;
        }
    }

    @PostMapping("/me/tracks")
    public ResponseEntity<Map<String, Object>> saveTrack(@RequestParam String trackId,
                                                          HttpSession session) {
        if (!userTokenService.isLoggedIn(session)) {
            return ResponseEntity.status(401)
                    .body(Map.of("error", "NOT_AUTHENTICATED",
                            "message", "Please log in with Spotify first."));
        }

        String userToken = userTokenService.getUserAccessToken(session);
        String trackUri = toTrackUri(trackId);

        try {
            saveWithFallback(userToken, trackId, trackUri);
            return ResponseEntity.ok(Map.of("saved", true, "trackId", trackId));
        } catch (RestClientResponseException ex) {
            if (ex.getStatusCode().value() == 401) {
                userTokenService.clearTokens(session);
                return ResponseEntity.status(401)
                        .body(Map.of("error", "NOT_AUTHENTICATED",
                                "message", "Spotify session expired. Please log in again."));
            }
            if (ex.getStatusCode().value() == 403) {
                return ResponseEntity.status(403)
                        .body(Map.of("error", "INSUFFICIENT_SCOPE",
                                "message", "Spotify authorization is missing required permissions. Please reconnect Spotify."));
            }
            throw ex;
        }
    }

    @DeleteMapping("/me/tracks")
    public ResponseEntity<Map<String, Object>> unsaveTrack(@RequestParam String trackId,
                                                            HttpSession session) {
        if (!userTokenService.isLoggedIn(session)) {
            return ResponseEntity.status(401)
                    .body(Map.of("error", "NOT_AUTHENTICATED",
                            "message", "Please log in with Spotify first."));
        }

        String userToken = userTokenService.getUserAccessToken(session);
        String trackUri = toTrackUri(trackId);

        try {
            unsaveWithFallback(userToken, trackId, trackUri);
            return ResponseEntity.ok(Map.of("saved", false, "trackId", trackId));
        } catch (RestClientResponseException ex) {
            if (ex.getStatusCode().value() == 401) {
                userTokenService.clearTokens(session);
                return ResponseEntity.status(401)
                        .body(Map.of("error", "NOT_AUTHENTICATED",
                                "message", "Spotify session expired. Please log in again."));
            }
            if (ex.getStatusCode().value() == 403) {
                return ResponseEntity.status(403)
                        .body(Map.of("error", "INSUFFICIENT_SCOPE",
                                "message", "Spotify authorization is missing required permissions. Please reconnect Spotify."));
            }
            throw ex;
        }
    }

    private void saveWithFallback(String userToken, String trackId, String trackUri) {
        try {
            spotifyClient.put()
                    .uri(uriBuilder -> uriBuilder.path("/v1/me/library")
                            .queryParam("uris", trackUri)
                            .build())
                    .header("Authorization", "Bearer " + userToken)
                    .header("Content-Type", "application/json")
                    .retrieve()
                    .toBodilessEntity();
        } catch (RestClientResponseException endpointEx) {
            if (endpointEx.getStatusCode().value() != 404) {
                throw endpointEx;
            }
            spotifyClient.put()
                    .uri(uriBuilder -> uriBuilder.path("/v1/me/tracks")
                            .queryParam("ids", trackId)
                            .build())
                    .header("Authorization", "Bearer " + userToken)
                    .header("Content-Type", "application/json")
                    .retrieve()
                    .toBodilessEntity();
        }
    }

    private void unsaveWithFallback(String userToken, String trackId, String trackUri) {
        try {
            spotifyClient.delete()
                    .uri(uriBuilder -> uriBuilder.path("/v1/me/library")
                            .queryParam("uris", trackUri)
                            .build())
                    .header("Authorization", "Bearer " + userToken)
                    .header("Content-Type", "application/json")
                    .retrieve()
                    .toBodilessEntity();
        } catch (RestClientResponseException endpointEx) {
            if (endpointEx.getStatusCode().value() != 404) {
                throw endpointEx;
            }
            spotifyClient.delete()
                    .uri(uriBuilder -> uriBuilder.path("/v1/me/tracks")
                            .queryParam("ids", trackId)
                            .build())
                    .header("Authorization", "Bearer " + userToken)
                    .header("Content-Type", "application/json")
                    .retrieve()
                    .toBodilessEntity();
        }
    }

    private Boolean[] callContains(String userToken, List<String> trackIds, List<String> trackUris) {
        try {
            return spotifyClient.get()
                    .uri(uriBuilder -> uriBuilder.path("/v1/me/library/contains")
                            .queryParam("uris", String.join(",", trackUris))
                            .build())
                    .header("Authorization", "Bearer " + userToken)
                    .retrieve()
                    .body(Boolean[].class);
        } catch (RestClientResponseException endpointEx) {
            if (endpointEx.getStatusCode().value() != 404) {
                throw endpointEx;
            }
            return spotifyClient.get()
                    .uri(uriBuilder -> uriBuilder.path("/v1/me/tracks/contains")
                            .queryParam("ids", String.join(",", trackIds))
                            .build())
                    .header("Authorization", "Bearer " + userToken)
                    .retrieve()
                    .body(Boolean[].class);
        }
    }

    private String toTrackUri(String trackId) {
        return "spotify:track:" + trackId;
    }
}

