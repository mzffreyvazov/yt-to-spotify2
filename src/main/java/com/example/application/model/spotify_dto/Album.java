package com.example.application.model.spotify_dto;

import java.util.List;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class Album {
    private List<Image> images; // List of album art images
    // We don't need to map other album fields like album_type, name, release_date, etc.
}