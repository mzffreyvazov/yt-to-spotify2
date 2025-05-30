package com.example.application.model.spotify_dto;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class Image {
    private String url; // This is our albumImageUrl
    private int height;
    private int width;
}
