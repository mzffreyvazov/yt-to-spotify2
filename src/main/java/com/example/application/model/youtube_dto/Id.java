package com.example.application.model.youtube_dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class Id {
    private String kind; // e.g., "youtube#video", "youtube#channel", "youtube#playlist"
    private String videoId; // Present if kind is "youtube#video"
    private String channelId; // Present if kind is "youtube#channel"
    private String playlistId; // Present if kind is "youtube#playlist"
}