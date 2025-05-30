package com.example.application.model.spotify_dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ExternalIds {
    private String isrc; // International Standard Recording Code, a unique identifier for the track // Universal Product Code, yet another identifier for the track
    
}
