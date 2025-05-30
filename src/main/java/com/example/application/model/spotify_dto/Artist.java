package com.example.application.model.spotify_dto;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class Artist {
    private String name; // This is our artistName
    // We don't need to map other artist fields like id, href, external_urls
}
