package com.example.application.controller;

import java.io.IOException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.Map;
import java.util.UUID;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.client.RestClient;

import com.example.application.config.SpotifyProperties;
import com.example.application.model.spotify_dto.SpotifyAuthResponse;
import com.example.application.service.SpotifyUserTokenService;

import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;

/**
 * Handles Spotify OAuth 2.0 Authorization Code Flow for user-level access.
 * Required for operations like saving tracks to the user's Liked Songs.
 */
@RestController
@RequestMapping("/api/spotify/auth")
public class SpotifyUserAuthController {

    private static final String SPOTIFY_AUTHORIZE_URL = "https://accounts.spotify.com/authorize";
    private static final String REQUIRED_SCOPE        = "user-library-modify";
    private static final String SESSION_STATE_KEY     = "spotifyOAuthState";

    private final SpotifyProperties spotifyProperties;
    private final SpotifyUserTokenService userTokenService;
    private final RestClient restClient;

    public SpotifyUserAuthController(SpotifyProperties spotifyProperties,
                                     SpotifyUserTokenService userTokenService) {
        this.spotifyProperties = spotifyProperties;
        this.userTokenService  = userTokenService;
        this.restClient        = RestClient.builder().build();
    }

    /**
     * Initiates the Spotify OAuth login.
     * Redirects the browser to Spotify's authorization page.
     */
    @GetMapping("/login")
    public void login(HttpSession session, HttpServletResponse response) throws IOException {
        String state = UUID.randomUUID().toString().replace("-", "");
        session.setAttribute(SESSION_STATE_KEY, state);

        String redirectUri = spotifyProperties.getRedirectUri();
        String clientId    = spotifyProperties.getClientId();

        String url = SPOTIFY_AUTHORIZE_URL
                + "?client_id="     + encode(clientId)
                + "&response_type=code"
                + "&redirect_uri="  + encode(redirectUri)
                + "&scope="         + encode(REQUIRED_SCOPE)
                + "&state="         + encode(state);

        response.sendRedirect(url);
    }

    /**
     * Handles the Spotify OAuth callback.
     * Exchanges the authorization code for tokens and stores them in the session.
     */
    @GetMapping("/callback")
    public void callback(@RequestParam(required = false) String code,
                         @RequestParam(required = false) String state,
                         @RequestParam(required = false) String error,
                         HttpSession session,
                         HttpServletResponse response) throws IOException {

        if (error != null) {
            response.sendRedirect("/?spotifyAuthError=" + encode(error));
            return;
        }

        String expectedState = (String) session.getAttribute(SESSION_STATE_KEY);
        if (expectedState == null || !expectedState.equals(state)) {
            response.sendRedirect("/?spotifyAuthError=state_mismatch");
            return;
        }
        session.removeAttribute(SESSION_STATE_KEY);

        String clientId     = spotifyProperties.getClientId();
        String clientSecret = spotifyProperties.getClientSecret();
        String redirectUri  = spotifyProperties.getRedirectUri();
        String authUrl      = spotifyProperties.getAuthUrl();

        String authHeader = "Basic " + Base64.getEncoder()
                .encodeToString((clientId + ":" + clientSecret).getBytes());

        String body = "grant_type=authorization_code"
                + "&code="          + encode(code)
                + "&redirect_uri="  + encode(redirectUri);

        try {
            SpotifyAuthResponse authResponse = restClient.post()
                    .uri(authUrl)
                    .header("Authorization", authHeader)
                    .header("Content-Type", "application/x-www-form-urlencoded")
                    .body(body)
                    .retrieve()
                    .body(SpotifyAuthResponse.class);

            if (authResponse == null || authResponse.getAccessToken() == null) {
                response.sendRedirect("/?spotifyAuthError=token_exchange_failed");
                return;
            }

            userTokenService.storeTokens(session, authResponse);
            response.sendRedirect("/?spotifyAuthSuccess=true");

        } catch (Exception e) {
            response.sendRedirect("/?spotifyAuthError=token_exchange_failed");
        }
    }

    /** Returns whether the current session has a valid Spotify user token. */
    @GetMapping("/status")
    public ResponseEntity<Map<String, Object>> status(HttpSession session) {
        boolean loggedIn = userTokenService.isLoggedIn(session);
        return ResponseEntity.ok(Map.of("loggedIn", loggedIn));
    }

    /** Clears the user's Spotify auth tokens from the session. */
    @PostMapping("/logout")
    public ResponseEntity<Map<String, Object>> logout(HttpSession session) {
        userTokenService.clearTokens(session);
        return ResponseEntity.ok(Map.of("loggedOut", true));
    }

    // -------------------------------------------------------------------------

    private static String encode(String value) {
        return URLEncoder.encode(value, StandardCharsets.UTF_8);
    }
}
