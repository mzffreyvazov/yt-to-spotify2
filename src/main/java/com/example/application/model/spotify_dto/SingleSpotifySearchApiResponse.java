package com.example.application.model.spotify_dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class SingleSpotifySearchApiResponse {
    private SingleTrackItem track;
}
