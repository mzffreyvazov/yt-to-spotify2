package com.example.application.model.youtube_dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class Id {
    private String kind; 
    private String videoId; 
    private String channelId; 
    private String playlistId; 
}