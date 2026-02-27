package com.example.application.service;

import java.util.Base64;

import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;

import com.example.application.config.SpotifyProperties;
import com.example.application.exception.UpstreamServiceException;
import com.example.application.model.spotify_dto.SpotifyAuthResponse;

import jakarta.servlet.http.HttpSession;

/**
 * Manages Spotify user-level access tokens stored in the HTTP session.
 * Required for user-specific operations like saving tracks to Liked Songs.
 */
@Service
public class SpotifyUserTokenService {

    private static final String SESSION_ACCESS_TOKEN  = "spotifyUserAccessToken";
    private static final String SESSION_REFRESH_TOKEN = "spotifyUserRefreshToken";
    private static final String SESSION_TOKEN_EXPIRY  = "spotifyUserTokenExpiry";
    private static final String SESSION_SCOPE         = "spotifyUserScope";
    private static final String REQUIRED_SCOPE        = "user-library-modify";

    private final SpotifyProperties spotifyProperties;
    private final RestClient restClient;

    public SpotifyUserTokenService(SpotifyProperties spotifyProperties) {
        this.spotifyProperties = spotifyProperties;
        this.restClient = RestClient.builder().build();
    }

    /** Returns true if the session contains a valid (non-expired) user access token. */
    public boolean isLoggedIn(HttpSession session) {
        String token = (String) session.getAttribute(SESSION_ACCESS_TOKEN);
        if (token == null) {
            return false;
        }
        Long expiry = (Long) session.getAttribute(SESSION_TOKEN_EXPIRY);
        return expiry != null
                && System.currentTimeMillis() < expiry
                && hasRequiredScope(session);
    }

    /**
     * Returns the user access token, refreshing it first if it has expired.
     * Throws IllegalStateException when the user is not authenticated at all.
     */
    public String getUserAccessToken(HttpSession session) {
        if (session.getAttribute(SESSION_ACCESS_TOKEN) == null) {
            throw new IllegalStateException("User not authenticated with Spotify");
        }

        if (!hasRequiredScope(session)) {
            clearTokens(session);
            throw new IllegalStateException("Spotify authorization is missing required scope; please log in again");
        }

        Long expiry = (Long) session.getAttribute(SESSION_TOKEN_EXPIRY);
        if (expiry != null && System.currentTimeMillis() >= expiry) {
            String refreshToken = (String) session.getAttribute(SESSION_REFRESH_TOKEN);
            if (refreshToken != null) {
                refreshTokens(session, refreshToken);
            } else {
                clearTokens(session);
                throw new IllegalStateException("User session expired; please log in again");
            }
        }

        return (String) session.getAttribute(SESSION_ACCESS_TOKEN);
    }

    /** Stores a new token set (access + optional refresh) in the session. */
    public void storeTokens(HttpSession session, SpotifyAuthResponse authResponse) {
        long expiry = System.currentTimeMillis() + ((long) authResponse.getExpiresIn() * 1000) - 30_000;
        session.setAttribute(SESSION_ACCESS_TOKEN, authResponse.getAccessToken());
        session.setAttribute(SESSION_TOKEN_EXPIRY, expiry);
        if (authResponse.getRefreshToken() != null) {
            session.setAttribute(SESSION_REFRESH_TOKEN, authResponse.getRefreshToken());
        }
        if (authResponse.getScope() != null) {
            session.setAttribute(SESSION_SCOPE, authResponse.getScope());
        }
    }

    /** Removes all user auth data from the session. */
    public void clearTokens(HttpSession session) {
        session.removeAttribute(SESSION_ACCESS_TOKEN);
        session.removeAttribute(SESSION_REFRESH_TOKEN);
        session.removeAttribute(SESSION_TOKEN_EXPIRY);
        session.removeAttribute(SESSION_SCOPE);
    }

    // -------------------------------------------------------------------------

    private void refreshTokens(HttpSession session, String refreshToken) {
        String clientId     = spotifyProperties.getClientId();
        String clientSecret = spotifyProperties.getClientSecret();
        String authUrl      = spotifyProperties.getAuthUrl();
        String currentScope = (String) session.getAttribute(SESSION_SCOPE);

        String authHeader = "Basic " + Base64.getEncoder()
                .encodeToString((clientId + ":" + clientSecret).getBytes());

        try {
            SpotifyAuthResponse response = restClient.post()
                    .uri(authUrl)
                    .header("Authorization", authHeader)
                    .header("Content-Type", "application/x-www-form-urlencoded")
                    .body("grant_type=refresh_token&refresh_token=" + refreshToken)
                    .retrieve()
                    .body(SpotifyAuthResponse.class);

            if (response == null || response.getAccessToken() == null) {
                clearTokens(session);
                throw new UpstreamServiceException("Failed to refresh Spotify user token");
            }

            // Spotify may issue a new refresh token; keep the old one if not
            if (response.getRefreshToken() == null) {
                response = new SpotifyAuthResponse(
                        response.getAccessToken(),
                        response.getTokenType(),
                        response.getExpiresIn(),
                        refreshToken,
                        response.getScope());
            }

            // Refresh responses may omit scope; preserve prior granted scope.
            if (response.getScope() == null && currentScope != null) {
                response = new SpotifyAuthResponse(
                        response.getAccessToken(),
                        response.getTokenType(),
                        response.getExpiresIn(),
                        response.getRefreshToken(),
                        currentScope);
            }

            storeTokens(session, response);
        } catch (Exception e) {
            clearTokens(session);
            throw new UpstreamServiceException("Failed to refresh Spotify user token", e);
        }
    }

    private boolean hasRequiredScope(HttpSession session) {
        String scope = (String) session.getAttribute(SESSION_SCOPE);
        if (scope == null || scope.isBlank()) {
            return false;
        }
        return java.util.Arrays.stream(scope.split("\\s+"))
                .anyMatch(REQUIRED_SCOPE::equals);
    }
}
