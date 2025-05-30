package com.example.application.model.youtube_dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class SearchItem {
    private String kind; // e.g., "YoutubeResult"
    private String etag;
    private Id id; // Contains the specific ID of the resource (video, channel, playlist)
    private Snippet snippet;
}
