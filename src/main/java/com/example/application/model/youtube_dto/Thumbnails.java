package com.example.application.model.youtube_dto;

import com.fasterxml.jackson.annotation.JsonProperty;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class Thumbnails {
    // Jackson needs @JsonProperty if the JSON field name is a reserved keyword like 'default'
    // or if it doesn't match the camelCase Java convention.
    @JsonProperty("default")
    private Thumbnail defaultThumbnail; // Renamed from 'default' to avoid potential conflicts
    private Thumbnail medium;
    private Thumbnail high;
    private Thumbnail standard; // Optional, depending on API response and your needs
    private Thumbnail maxres;   // Optional
}