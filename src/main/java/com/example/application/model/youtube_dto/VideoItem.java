package com.example.application.model.youtube_dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class VideoItem {
    private String kind;
    private String etag;
    private String id;  // Simple string, not an object
    private Snippet snippet;
    private ContentDetails contentDetails;
    private Statistics statistics;
}