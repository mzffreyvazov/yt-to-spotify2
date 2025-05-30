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
    private String id; 
    private String name; 

    private AlbumSearch album; 
    private List<Artist> artists; 
    private String preview_url; 

    @JsonProperty("external_urls") 
    private ExternalUrls externalUrls;

}
