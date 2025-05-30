package com.example.application.model.response;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class SpotifyResponse {
    
    private String trackId;     // Corresponds to id
    private String songTitle;   // Corresponds to name
    private String artistName;  // Corresponds to artists[0].name
    private String albumImageUrl; // Corresponds to album.images[0].url (or other size)
    private String previewUrl;  // Corresponds to preview_url
    private String spotifyUrl;
}
