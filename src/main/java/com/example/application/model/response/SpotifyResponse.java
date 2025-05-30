package com.example.application.model.response;

import java.util.ArrayList;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class SpotifyResponse {
    
    private String trackId;
    private String trackName;
    private String artistName;
    private String albumName;
    private String channelTitle;
    private String thumbnailUrl;
    private ArrayList<String> tags;
}
