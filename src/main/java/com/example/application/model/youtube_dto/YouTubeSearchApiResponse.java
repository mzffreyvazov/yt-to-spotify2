package com.example.application.model.youtube_dto;

import java.util.List;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class YouTubeSearchApiResponse {
    
    private String kind; // e.g., "YoutubeListResponse"
    private String etag;
    private String nextPageToken; // Token for fetching the next page of results
    private String regionCode;
    private PageInfo pageInfo; // Contains totalResults and resultsPerPage
    private List<SearchItem> items; 
}
