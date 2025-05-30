package com.example.application.model.spotify_dto;

import java.util.List;

import com.fasterxml.jackson.annotation.JsonProperty;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class TrackItem {
    private String id; // This is our trackId
    private String name; // This is our songTitle

    private Album album; // Contains album details, including images
    private List<Artist> artists; // List of artists for the track
    private String preview_url; // This is our previewUrl

    @JsonProperty("external_urls") // To map "external_urls" JSON key to externalUrls field
    private ExternalUrls externalUrls;
    // We don't need to map all other fields like disc_number, duration_ms, explicit, etc.,
    // if we don't plan to use them.
}
