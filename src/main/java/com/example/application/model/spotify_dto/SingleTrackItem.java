package com.example.application.model.spotify_dto;

import com.fasterxml.jackson.annotation.JsonProperty;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class SingleTrackItem {
    @JsonProperty("id") // To map "id" JSON key to trackId field
    private String id;

    private String name; 
    private int duration_ms;
    private Artist artist; // This is our artistName
    private AlbumTrack album; // This is our albumName

    @JsonProperty("preview_url") // To map "preview_url" JSON key to previewUrl field
    private String previewUrl;

    @JsonProperty("external_ids") // To map "external_urls" JSON key to externalUrls field
    private ExternalUrls externalIds;

    @JsonProperty("external_urls") // To map "external_urls" JSON key to externalUrls field
    private ExternalUrls externalUrls;
}
