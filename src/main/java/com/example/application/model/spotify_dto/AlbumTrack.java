package com.example.application.model.spotify_dto;

import java.util.List;

import com.fasterxml.jackson.annotation.JsonProperty;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class AlbumTrack {
    @JsonProperty("release_date")   
    private String releaseDate; 

    private List<Image> images;
}