package com.example.application.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import com.example.application.model.queries.SpotifySearchQuery;
import com.example.application.model.response.SpotifyResponse;
import com.example.application.model.response.YoutubeResponse;
import com.example.application.service.LinkConvertorService;
import com.example.application.service.LinkProcessorService;
import com.example.application.service.SpotifyService;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/links")
public class LinkProcessingController {
    
    private final LinkConvertorService linkConverterService;
    private final LinkProcessorService linkProcessorService;
    private final SpotifyService spotifyService;
    
    public LinkProcessingController(LinkConvertorService linkConverterService, 
                                  LinkProcessorService linkProcessorService,
                                  SpotifyService spotifyService) {
        this.linkConverterService = linkConverterService;
        this.linkProcessorService = linkProcessorService;
        this.spotifyService = spotifyService;
    }
    
    /**
     * Endpoint to analyze a link and determine its type
     */
    @GetMapping("/analyze")
    public ResponseEntity<Map<String, String>> analyzeLink(@RequestParam String url) {
        String linkType = linkConverterService.detectLinkType(url);
        String id = null;
        
        if ("YOUTUBE".equals(linkType)) {
            id = linkConverterService.extractYoutubeId(url);
        } else if ("SPOTIFY".equals(linkType)) {
            id = linkConverterService.extractSpotifyId(url);
        }
        
        Map<String, String> response = new HashMap<>();
        response.put("linkType", linkType);
        response.put("id", id);
        response.put("url", url);
        
        return ResponseEntity.ok(response);
    }
    
    /**
     * Endpoint to convert a YouTube link to a Spotify search query
     */
    @GetMapping("/youtube-to-spotify")
    public ResponseEntity<SpotifySearchQuery> convertYoutubeToSpotify(@RequestParam String youtubeUrl) {
        try {
            SpotifySearchQuery query = linkConverterService.youtubeToSpotifyQuery(youtubeUrl);
            return ResponseEntity.ok(query);
        } catch (Exception e) {
            return ResponseEntity.badRequest().build();
        }
    }
    
    /**
     * Endpoint to test the raw query string that would be sent to Spotify
     */
    @GetMapping("/test-query")
    public ResponseEntity<Map<String, String>> testSpotifyQuery(@RequestParam String youtubeUrl) {
        try {
            SpotifySearchQuery query = linkConverterService.youtubeToSpotifyQuery(youtubeUrl);
            Map<String, String> response = new HashMap<>();
            response.put("title", query.getTitle());
            response.put("artist", query.getArtist());
            response.put("album", query.getAlbum());
            response.put("spotifyQueryString", query.toQueryString());
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            Map<String, String> errorResponse = new HashMap<>();
            errorResponse.put("error", e.getMessage());
            return ResponseEntity.badRequest().body(errorResponse);
        }
    }
    
    /**
     * Endpoint to extract a YouTube video ID from a URL
     */
    @GetMapping("/extract-youtube-id")
    public ResponseEntity<Map<String, String>> extractYoutubeId(@RequestParam String url) {
        String videoId = linkConverterService.extractYoutubeId(url);
        Map<String, String> response = new HashMap<>();
        
        if (videoId != null) {
            response.put("videoId", videoId);
            return ResponseEntity.ok(response);
        } else {
            response.put("error", "Could not extract YouTube video ID from the provided URL");
            return ResponseEntity.badRequest().body(response);
        }
    }
    
    /**
     * Complete endpoint that converts a YouTube link to Spotify tracks
     * Uses the enhanced LinkProcessorService with fallback search strategies
     */
    @GetMapping("/youtube-to-spotify-tracks")
    public ResponseEntity<List<SpotifyResponse>> findSpotifyTracks(@RequestParam String youtubeUrl) {
        try {
            List<SpotifyResponse> results = linkProcessorService.processYoutubeLink(youtubeUrl);
            if (results.isEmpty()) {
                System.out.println("Warning: No Spotify tracks found for: " + youtubeUrl);
            } else {
                System.out.println("Found " + results.size() + " Spotify tracks for: " + youtubeUrl);
            }
            return ResponseEntity.ok(results);
        } catch (Exception e) {
            System.err.println("Error processing YouTube to Spotify conversion: " + e.getMessage());
            e.printStackTrace();
            return ResponseEntity.badRequest().build();
        }
    }
    
    /**
     * A more detailed endpoint that returns both the query and the results
     * Also uses the enhanced query generation and fallback strategies
     */
    @GetMapping("/youtube-to-spotify-detailed")
    public ResponseEntity<Map<String, Object>> findSpotifyTracksDetailed(@RequestParam String youtubeUrl) {
        try {
            SpotifySearchQuery query = linkConverterService.youtubeToSpotifyQuery(youtubeUrl);
            String queryString = query.toQueryString();
            String generalQueryString = query.toGeneralQueryString();
            
            List<SpotifyResponse> results = linkProcessorService.processYoutubeLink(youtubeUrl);
            
            Map<String, Object> response = new HashMap<>();
            response.put("query", query);
            response.put("specificQueryString", queryString);
            response.put("generalQueryString", generalQueryString);
            response.put("results", results);
            response.put("youtubeUrl", youtubeUrl);
            response.put("count", results.size());
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("error", e.getMessage());
            errorResponse.put("youtubeUrl", youtubeUrl);
            return ResponseEntity.badRequest().body(errorResponse);
        }
    }
    
    /**
     * Endpoint that provides unbiased search results without personalization
     * Uses specific techniques to avoid personalization bias
     */
    @GetMapping("/youtube-to-spotify-unbiased")
    public ResponseEntity<List<SpotifyResponse>> findUnbiasedSpotifyTracks(@RequestParam String youtubeUrl) {
        try {
            SpotifySearchQuery query = linkConverterService.youtubeToSpotifyQuery(youtubeUrl);
            // Use a more generic search query to avoid personalization
            String queryString = query.toGeneralQueryString();
            
            // Add a random offset to the search to avoid always getting the same top results
            // int randomOffset = (int)(Math.random() * 5);
            
            // Log the search for debugging
            System.out.println("Performing unbiased search with query: " + queryString);
            
            List<SpotifyResponse> results = spotifyService.getSpotifyResponse(queryString);
            System.out.println("Found " + results.size() + " results for unbiased search");
            return ResponseEntity.ok(results);
        } catch (Exception e) {
            System.err.println("Error processing unbiased search: " + e.getMessage());
            return ResponseEntity.badRequest().build();
        }
    }


    @GetMapping("/spotify-to-youtube")
    public ResponseEntity<List<YoutubeResponse>> findYoutubeTracks(@RequestParam String spotifyUrl) {
        try {
            List<YoutubeResponse> results = linkProcessorService.processSpotifyLink(spotifyUrl);
            if (results.isEmpty()) {
                System.out.println("Warning: No Youtube tracks found for: " + spotifyUrl);
            } else {
                System.out.println("Found " + results.size() + " Youtube tracks for: " + spotifyUrl);
            }
            return ResponseEntity.ok(results);
        } catch (Exception e) {
            System.err.println("Error processing Spotify to YouTube conversion: " + e.getMessage());
            e.printStackTrace();
            return ResponseEntity.badRequest().build();
        }
    }

    @GetMapping("/spotify-to-youtube-detailed")
    public ResponseEntity<Map<String, Object>> findYoutubeTracksDetailed(@RequestParam String spotifyUrl) {
        try {
            com.example.application.model.queries.YoutubeSearchQuery query = linkConverterService.spotifyToYoutubeQuery(spotifyUrl);
            String queryString = query.toQueryString();
            String generalQueryString = query.toGeneralQueryString();
            
            List<YoutubeResponse> results = linkProcessorService.processSpotifyLink(spotifyUrl);
            
            Map<String, Object> response = new HashMap<>();
            response.put("query", query);
            response.put("specificQueryString", queryString);
            response.put("generalQueryString", generalQueryString);
            response.put("results", results);
            response.put("spotifyUrl", spotifyUrl);
            response.put("count", results.size());
            
            // Add additional search metadata
            Map<String, String> searchMetadata = new HashMap<>();
            searchMetadata.put("searchType", "track");
            searchMetadata.put("resultLimit", "5");
            searchMetadata.put("includeCovers", "true");
            response.put("searchMetadata", searchMetadata);
            
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("error", e.getMessage());
            errorResponse.put("spotifyUrl", spotifyUrl);
            return ResponseEntity.badRequest().body(errorResponse);
        }
    }
}
