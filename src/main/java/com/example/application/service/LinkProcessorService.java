package com.example.application.service;

import org.springframework.stereotype.Service;
import com.example.application.model.queries.SpotifySearchQuery;
import com.example.application.model.response.SpotifyResponse;
import reactor.core.publisher.Mono;
import java.util.List;
import java.util.logging.Logger;

@Service
public class LinkProcessorService {
    
    private static final Logger LOGGER = Logger.getLogger(LinkProcessorService.class.getName());
    
    private SpotifyService spotifyService;
    private YoutubeService youtubeService;
    private LinkConvertorService linkConvertor;
    
    public LinkProcessorService(SpotifyService spotifyService, YoutubeService youtubeService, LinkConvertorService linkConvertor) {
        this.spotifyService = spotifyService;
        this.youtubeService = youtubeService;
        this.linkConvertor = linkConvertor;
    }

    /**
     * Processes a YouTube link and finds matching Spotify tracks
     * Implements fallback search strategies if the initial search returns no results
     */
    public Mono<List<SpotifyResponse>> processYoutubeLink(String youtubeUrl) {
        LOGGER.info("Processing YouTube URL: " + youtubeUrl);
        
        return linkConvertor.youtubeToSpotifyQuery(youtubeUrl)
                .flatMap(this::searchSpotifyWithFallbacks);
    }
    
    /**
     * Search Spotify with progressive fallback strategies
     */
    private Mono<List<SpotifyResponse>> searchSpotifyWithFallbacks(SpotifySearchQuery query) {
        // Strategy 1: Use field-specific search (track: artist:)
        String specificQuery = query.toQueryString();
        LOGGER.info("Searching Spotify with specific query: " + specificQuery);
        
        return spotifyService.getSpotifyResponse(specificQuery)
                .flatMap(results -> {
                    if (results == null || results.isEmpty()) {
                        // Strategy 2: Try with a general search (no field prefixes)
                        String generalQuery = query.toGeneralQueryString();
                        LOGGER.info("No results with specific query, trying general query: " + generalQuery);
                        return spotifyService.getSpotifyResponse(generalQuery);
                    }
                    return Mono.just(results);
                })
                .flatMap(results -> {
                    if (results == null || results.isEmpty()) {
                        // Strategy 3: Try with just the track title if artist search might be limiting results
                        String titleOnlyQuery = "track:" + query.getTitle();
                        LOGGER.info("No results with general query, trying title-only query: " + titleOnlyQuery);
                        return spotifyService.getSpotifyResponse(titleOnlyQuery);
                    }
                    return Mono.just(results);
                });
    }
}
