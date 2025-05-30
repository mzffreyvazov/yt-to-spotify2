package com.example.application.model.youtube_dto;

import java.util.List;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class YouTubeSearchApiResponse {
    
    private String kind; 
    private String etag;
    private String nextPageToken; 
    private String regionCode;
    private PageInfo pageInfo; 
    private List<SearchItem> items; 
}
