package com.example.application.model.youtube_dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class Snippet {
    private String publishedAt; 
    private String channelId;
    private String title;
    private String description;
    private Thumbnails thumbnails; 
    private String channelTitle;
    private String liveBroadcastContent; 
    
}