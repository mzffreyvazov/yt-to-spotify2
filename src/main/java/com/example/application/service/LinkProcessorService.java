package com.example.application.service;

import org.springframework.stereotype.Service;
import com.example.application.model.queries.SpotifySearchQuery;
import com.example.application.model.queries.YoutubeSearchQuery;
import com.example.application.model.response.SpotifyResponse;
import com.example.application.model.response.YoutubeResponse;
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
     * Processes a Spotify link and finds matching Youtube tracks
     * Implements fallback search strategies if the initial search returns no results
     */

    public Mono<List<YoutubeResponse>> processSpotifyLink(String spotifyUrl) {
        LOGGER.info("Processing Spotify URL: " + spotifyUrl);
        
        return linkConvertor.spotifyToYoutubeQuery(spotifyUrl)
                .flatMap(this::searchYoutubeWithFallbacks);
    }

    /**
     * Search Youtube with progressive fallback strategies
     */

    private Mono<List<YoutubeResponse>> searchYoutubeWithFallbacks(YoutubeSearchQuery query) {
        
        String specificQuery = query.toQueryString();
        LOGGER.info("Searching YouTube with specific query: " + specificQuery);
        
        return youtubeService.getYoutubeResponse(specificQuery)
                .flatMap(results -> {
                    if (results == null || results.isEmpty()) {
                        
                        String generalQuery = query.toGeneralQueryString();
                        LOGGER.info("No results with specific query, trying general query: " + generalQuery);
                        return youtubeService.getYoutubeResponse(generalQuery);
                    }
                    return Mono.just(results);
                })
                .flatMap(results -> {
                    if (results == null || results.isEmpty()) {
                        
                        String titleOnlyQuery = "track:" + query.getTitle();
                        LOGGER.info("No results with general query, trying title-only query: " + titleOnlyQuery);
                        return youtubeService.getYoutubeResponse(titleOnlyQuery);                    }
                    return Mono.just(results);
                })
                .map(results -> {
                    // SIMILARITY ALGORITHM DISABLED - Returning all results in original API order
                    // List<YoutubeResponse> sorted = sortResultsByRelevanceYT(results, query);
                    System.out.println("\n========== FINAL RESULTS (Original API Order - No Filtering) ==========");
                    System.out.println("Total results: " + results.size());
                    for (int i = 0; i < results.size(); i++) {
                        YoutubeResponse video = results.get(i);
                        System.out.println("  " + (i + 1) + ". " + video.getSongTitle() + " - " + video.getArtistName());
                        System.out.println("     URL: https://www.youtube.com/watch?v=" + video.getVideoId());
                    }
                    System.out.println("================================================================\n");
                    return results;
                });
    }

    
    /**
     * Search Spotify with progressive fallback strategies
     */
    private Mono<List<SpotifyResponse>> searchSpotifyWithFallbacks(SpotifySearchQuery query) {
        
        String specificQuery = query.toQueryString();
        LOGGER.info("Searching Spotify with specific query: " + specificQuery);
        
        return spotifyService.getSpotifyResponse(specificQuery)
                .flatMap(results -> {
                    if (results == null || results.isEmpty()) {
                        
                        String generalQuery = query.toGeneralQueryString();
                        LOGGER.info("No results with specific query, trying general query: " + generalQuery);
                        return spotifyService.getSpotifyResponse(generalQuery);
                    }
                    return Mono.just(results);
                })
                .flatMap(results -> {
                    if (results == null || results.isEmpty()) {
                        
                        String titleOnlyQuery = "track:" + query.getTitle();
                        LOGGER.info("No results with general query, trying title-only query: " + titleOnlyQuery);
                        return spotifyService.getSpotifyResponse(titleOnlyQuery);
                    }
                    return Mono.just(results);                })
                .map(results -> {
                    // SIMILARITY ALGORITHM DISABLED - Returning all results in original API order
                    // List<SpotifyResponse> sorted = sortResultsByRelevanceSP(results, query);
                    System.out.println("\n========== FINAL RESULTS (Original API Order - No Filtering) ==========");
                    System.out.println("Total results: " + results.size());
                    for (int i = 0; i < results.size(); i++) {
                        SpotifyResponse track = results.get(i);
                        System.out.println("  " + (i + 1) + ". " + track.getSongTitle() + " - " + track.getArtistName());
                        System.out.println("     URL: " + track.getSpotifyUrl());
                    }
                    System.out.println("================================================================\n");
                    return results;
                });
    }
    

}
