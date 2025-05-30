package com.example.application.service;

import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;

import com.example.application.model.response.YoutubeResponse;
import com.example.application.model.youtube_dto.SearchItem;
import com.example.application.model.youtube_dto.VideoItem;
import com.example.application.model.youtube_dto.YouTubeSearchApiResponse;
import com.example.application.model.youtube_dto.YouTubeVideoApiResponse;

import reactor.core.publisher.Mono;

@Service
public class YoutubeService {
    
    @Value("${YOUTUBE_API_KEY}")
    private String YT_API_KEY;

    // injecting web client
    private WebClient searchWebClientYt;
    private WebClient trackWebClientYt;


    // api parameters
    private static final int MAX_RESULTS = 10;
    private static final String orderUrlParam = "relevance";
    private static final String partUrlParam = "snippet";
    private static final String typeUrlParam = "video";


    public YoutubeService(@Qualifier("searchWebClientYoutube") WebClient searchWebClient,
                          @Qualifier("trackWebClientYoutube") WebClient trackWebClient) {
        this.searchWebClientYt = searchWebClient;
        this.trackWebClientYt = trackWebClient; // Assuming you want to use the search WebClient for YouTube searches

    }

    public Mono<List<YoutubeResponse>> getYoutubeResponse(String searchQuery) {
        
        return searchWebClientYt.get()
                .uri(uriBuilder -> uriBuilder
                        // .path(baseUrl)
                        .queryParam("key", YT_API_KEY)
                        .queryParam("q", searchQuery)
                        .queryParam("part", partUrlParam)
                        .queryParam("type", typeUrlParam)
                        .queryParam("order", orderUrlParam)
                        .queryParam("maxResults", MAX_RESULTS)
                        .build())
                .retrieve()
                .bodyToMono(YouTubeSearchApiResponse.class)
                .map(apiResponse -> {
                    if (apiResponse == null || apiResponse.getItems() == null) {
                        return Collections.emptyList(); // Return an empty list if no data
                    }
                    // Map the API response to a list of YoutubeResponse objects
                    return apiResponse.getItems().stream()
                        .filter(item -> item.getId() != null && "youtube#video".equals(item.getId().getKind()))
                        .map(this::mapSearchItemToYoutubeResponse)
                        .filter(Objects::nonNull) // Filter out any null responses
                        .collect((Collectors.toList()));
                });
    }   

    public Mono<YoutubeResponse> getSingleVideo(String videoId) {
        return trackWebClientYt.get()
                .uri(uriBuilder -> uriBuilder
                        .queryParam("key", YT_API_KEY)
                        .queryParam("id", videoId)
                        .queryParam("part", "snippet,contentDetails,statistics")  // Request additional parts
                        .build())
                .retrieve()
                .bodyToMono(YouTubeVideoApiResponse.class)  // Use the new response class
                .map(apiResponse -> {
                    if (apiResponse == null || apiResponse.getItems() == null || apiResponse.getItems().isEmpty()) {
                        return null; // Return null if no data found
                    }
                    
                    // Get the first item (should be the only one since we're querying by ID)
                    VideoItem item = apiResponse.getItems().get(0);
                    return mapVideoItemToYoutubeResponse(item);
                })
                .switchIfEmpty(Mono.error(new RuntimeException("Video not found")));
    }


    private YoutubeResponse mapVideoItemToYoutubeResponse(VideoItem item) {
        if (item == null || item.getSnippet() == null) {
            return null;
        }
        
        String videoId = item.getId();  // Direct string, not an object property
        String songTitle = item.getSnippet().getTitle();
        String artistName = item.getSnippet().getChannelTitle();
        String description = item.getSnippet().getDescription();
        
        String thumbnailUrl = null;
        if (item.getSnippet().getThumbnails() != null) {
            // Try to get the high quality thumbnail, or fallback to others
            if (item.getSnippet().getThumbnails().getHigh() != null) {
                thumbnailUrl = item.getSnippet().getThumbnails().getHigh().getUrl();
            } else if (item.getSnippet().getThumbnails().getMedium() != null) {
                thumbnailUrl = item.getSnippet().getThumbnails().getMedium().getUrl();
            } else if (item.getSnippet().getThumbnails().getDefaultThumbnail() != null) {
                thumbnailUrl = item.getSnippet().getThumbnails().getDefaultThumbnail().getUrl();
            }
        }
        
        return new YoutubeResponse(videoId, songTitle, artistName, thumbnailUrl, description);
    }


    private YoutubeResponse mapSearchItemToYoutubeResponse(SearchItem item) {
        // Perform basic null checks to prevent NullPointerExceptions
        if (item == null || item.getId() == null || item.getSnippet() == null) {
            return null; // Cannot map if core parts are missing
        }

        String videoId = item.getId().getVideoId();
        String songTitle = item.getSnippet().getTitle();
        // Commonly, channelTitle is used for artistName in search results if no direct artist field exists
        String artistName = item.getSnippet().getChannelTitle();
        String description = item.getSnippet().getDescription();

        String thumbnailUrl = null;
        // Logic to select the best available thumbnail URL
        if (item.getSnippet().getThumbnails() != null) {
            if (item.getSnippet().getThumbnails().getHigh() != null) {
                thumbnailUrl = item.getSnippet().getThumbnails().getHigh().getUrl();
            } else if (item.getSnippet().getThumbnails().getMedium() != null) {
                thumbnailUrl = item.getSnippet().getThumbnails().getMedium().getUrl();
            } else if (item.getSnippet().getThumbnails().getDefaultThumbnail() != null) {
                thumbnailUrl = item.getSnippet().getThumbnails().getDefaultThumbnail().getUrl();
            }
        }

        // Create and return an instance of your simplified YoutubeResponse DTO
        return new YoutubeResponse(videoId, songTitle, artistName, thumbnailUrl, description);
    }
    

}
