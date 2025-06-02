package com.example.application.model.response;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class YoutubeResponse {

    private String videoId; //videoId in id
    private String songTitle; // title in snippet
    private String artistName; // channelTitle in snippet
    private String thumbnailUrl; // url in snipper.thumbnails
    private String description; // description in snippet

}
