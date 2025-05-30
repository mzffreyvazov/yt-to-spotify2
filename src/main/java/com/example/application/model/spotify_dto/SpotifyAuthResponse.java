package com.example.application.model.spotify_dto;

import com.fasterxml.jackson.annotation.JsonProperty;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data // Lombok annotation to generate getters, setters, equals, hashCode, toString
@NoArgsConstructor // Lombok annotation to generate a no-argument constructor
@AllArgsConstructor // Lombok annotation to generate a constructor with all arguments
public class SpotifyAuthResponse {

    // Maps the "access_token" JSON field to this Java field
    @JsonProperty("access_token")
    private String accessToken;

    // Maps the "token_type" JSON field (e.g., "Bearer")
    @JsonProperty("token_type")
    private String tokenType;

    // Maps the "expires_in" JSON field (seconds until the token expires)
    @JsonProperty("expires_in")
    private int expiresIn;

    // Spotify might also return a "scope" field, but it's often not strictly needed for client credentials
    // @JsonProperty("scope")
    // private String scope;
}