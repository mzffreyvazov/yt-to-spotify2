package com.example.application.service;

import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;

import com.example.application.model.response.YoutubeResponse;
import com.example.application.model.youtube_dto.SearchItem;
import com.example.application.model.youtube_dto.YouTubeSearchApiResponse;

import reactor.core.publisher.Mono;

@Service
public class YoutubeService {
    
    @Value("${YOUTUBE_API_KEY}")
    private String YT_API_KEY;

    // injecting web client
    private WebClient webClient;


    // api parameters
    private static final int MAX_RESULTS = 10;
    private static final String orderUrlParam = "relevance";
    private static final String partUrlParam = "snippet";
    private static final String typeUrlParam = "video";
    private static final String baseUrl = "https://www.googleapis.com/youtube/v3/search";

    public YoutubeService(@Autowired WebClient.Builder webClientBuilder) {
        this.webClient = webClientBuilder.baseUrl(baseUrl).build();
    }

    public Mono<List<YoutubeResponse>> getYoutubeResponse(String searchQuery) {
        
        return webClient.get()
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
