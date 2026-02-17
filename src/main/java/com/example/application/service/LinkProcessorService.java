package com.example.application.service;

import org.springframework.stereotype.Service;
import com.example.application.model.queries.SpotifySearchQuery;
import com.example.application.model.queries.YoutubeSearchQuery;
import com.example.application.model.response.SpotifyResponse;
import com.example.application.model.response.YoutubeResponse;
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
    public List<SpotifyResponse> processYoutubeLink(String youtubeUrl) {
        LOGGER.info("Processing YouTube URL: " + youtubeUrl);
        
        SpotifySearchQuery query = linkConvertor.youtubeToSpotifyQuery(youtubeUrl);
        return searchSpotifyWithFallbacks(query);
    }

    /**
     * Processes input for YouTube -> Spotify mode.
     * - If input is a YouTube link, converts to Spotify query as before.
     * - If input is plain keywords, directly searches Spotify.
     */
    public List<SpotifyResponse> processYoutubeInput(String input) {
        String linkType = linkConvertor.detectLinkType(input);
        if ("YOUTUBE".equals(linkType)) {
            return processYoutubeLink(input);
        }
        if ("UNKNOWN".equals(linkType)) {
            return searchSpotifyByKeyword(input);
        }
        throw new IllegalArgumentException("Please provide a YouTube link or plain keywords");
    }
    
    /**
     * Processes a Spotify link and finds matching Youtube tracks
     * Implements fallback search strategies if the initial search returns no results
     */

    public List<YoutubeResponse> processSpotifyLink(String spotifyUrl) {
        LOGGER.info("Processing Spotify URL: " + spotifyUrl);
        
        YoutubeSearchQuery query = linkConvertor.spotifyToYoutubeQuery(spotifyUrl);
        return searchYoutubeWithFallbacks(query);
    }

    /**
     * Processes input for Spotify -> YouTube mode.
     * - If input is a Spotify link, converts to YouTube query as before.
     * - If input is plain keywords, directly searches YouTube.
     */
    public List<YoutubeResponse> processSpotifyInput(String input) {
        String linkType = linkConvertor.detectLinkType(input);
        if ("SPOTIFY".equals(linkType)) {
            return processSpotifyLink(input);
        }
        if ("UNKNOWN".equals(linkType)) {
            return searchYoutubeByKeyword(input);
        }
        throw new IllegalArgumentException("Please provide a Spotify link or plain keywords");
    }

    private List<SpotifyResponse> searchSpotifyByKeyword(String keywords) {
        String query = keywords == null ? "" : keywords.trim();
        LOGGER.info("Processing plain keywords for Spotify search: " + query);
        if (query.isEmpty()) {
            return List.of();
        }
        return spotifyService.getSpotifyResponse(query);
    }

    private List<YoutubeResponse> searchYoutubeByKeyword(String keywords) {
        String query = keywords == null ? "" : keywords.trim();
        LOGGER.info("Processing plain keywords for YouTube search: " + query);
        if (query.isEmpty()) {
            return List.of();
        }
        return youtubeService.getYoutubeResponse(query);
    }

    /**
     * Search Youtube with progressive fallback strategies
     */

    private List<YoutubeResponse> searchYoutubeWithFallbacks(YoutubeSearchQuery query) {
        
        String specificQuery = query.toQueryString();
        LOGGER.info("Searching YouTube with specific query: " + specificQuery);
        
        List<YoutubeResponse> results = youtubeService.getYoutubeResponse(specificQuery);
        
        if (results == null || results.isEmpty()) {
            String generalQuery = query.toGeneralQueryString();
            LOGGER.info("No results with specific query, trying general query: " + generalQuery);
            results = youtubeService.getYoutubeResponse(generalQuery);
        }
        
        if (results == null || results.isEmpty()) {
            String titleOnlyQuery = "track:" + query.getTitle();
            LOGGER.info("No results with general query, trying title-only query: " + titleOnlyQuery);
            results = youtubeService.getYoutubeResponse(titleOnlyQuery);
        }
        
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
    }

    
    /**
     * Search Spotify with progressive fallback strategies
     */
    private List<SpotifyResponse> searchSpotifyWithFallbacks(SpotifySearchQuery query) {
        
        String specificQuery = query.toQueryString();
        LOGGER.info("Searching Spotify with specific query: " + specificQuery);
        
        List<SpotifyResponse> results = spotifyService.getSpotifyResponse(specificQuery);
        
        if (results == null || results.isEmpty()) {
            String generalQuery = query.toGeneralQueryString();
            LOGGER.info("No results with specific query, trying general query: " + generalQuery);
            results = spotifyService.getSpotifyResponse(generalQuery);
        }
        
        if (results == null || results.isEmpty()) {
            String titleOnlyQuery = "track:" + query.getTitle();
            LOGGER.info("No results with general query, trying title-only query: " + titleOnlyQuery);
            results = spotifyService.getSpotifyResponse(titleOnlyQuery);
        }
        
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
    }
    

}
