package com.example.application.model.youtube_dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class Snippet {
    private String publishedAt; // ISO 8601 format date/time
    private String channelId;
    private String title;
    private String description;
    private Thumbnails thumbnails; // Object containing different thumbnail sizes
    private String channelTitle;
    private String liveBroadcastContent; // e.g., "none", "live", "upcoming"
    // Other fields can be added here if needed from the snippet (e.g., tags, categoryId, etc.)
}