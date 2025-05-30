package com.example.application.model.spotify_dto;

import com.fasterxml.jackson.annotation.JsonProperty;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class SingleTrackItem {
    @JsonProperty("id") 
    private String id;

    private String name; 
    private int duration_ms;
    private Artist artist; 
    private AlbumTrack album; 

    @JsonProperty("preview_url") 
    private String previewUrl;

    @JsonProperty("external_ids") 
    private ExternalUrls externalIds;

    @JsonProperty("external_urls") 
    private ExternalUrls externalUrls;
}
