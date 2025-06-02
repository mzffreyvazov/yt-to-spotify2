package com.example.application.service;

import org.apache.commons.text.similarity.JaroWinklerSimilarity;
import org.springframework.stereotype.Service;
import com.example.application.model.queries.SpotifySearchQuery;
import com.example.application.model.queries.YoutubeSearchQuery;
import com.example.application.model.response.SpotifyResponse;
import com.example.application.model.response.YoutubeResponse;
import org.apache.commons.text.similarity.LevenshteinDistance;
import org.apache.commons.text.similarity.JaroWinklerSimilarity;
import java.util.Comparator;
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
                .map(results -> sortResultsByRelevanceYT(results, query));
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
                .map(results -> sortResultsByRelevanceSP(results, query));
    }
    
    /**
     * Sort and filter results to prioritize the most relevant matches based on objective criteria
     * This helps avoid personalization bias in search results
     */
// Replace the existing sortResultsByRelevanceSP method
private List<SpotifyResponse> sortResultsByRelevanceSP(List<SpotifyResponse> results, SpotifySearchQuery query) {
    if (results == null || results.isEmpty()) {
        return results;
    }

    // Create similarity calculators
    LevenshteinDistance levenshtein = new LevenshteinDistance();
    JaroWinklerSimilarity jaroWinkler = new JaroWinklerSimilarity();    // Sort by multiple criteria
    results.sort((a, b) -> {
        // Calculate combined similarity scores for both results
        double scoreA = calculateSpotifyRelevanceScore(a, query, levenshtein, jaroWinkler);
        double scoreB = calculateSpotifyRelevanceScore(b, query, levenshtein, jaroWinkler);
        
        // Sort by similarity score (higher score first)
        return Double.compare(scoreB, scoreA);
    });

    // Keep only results above certain similarity threshold
    double threshold = 0.4; // Adjust this value based on testing
    return results.stream()
            .filter(result -> {
                double similarity = calculateRelevanceScore(
                    result.getSongTitle().toLowerCase(),
                    query.getTitle().toLowerCase(),
                    levenshtein,
                    jaroWinkler
                );
                return similarity >= threshold;
            })
            .limit(5) // Limit to top 5 most relevant results
            .toList();
}

// Add this helper method
private double calculateRelevanceScore(String title1, String title2, 
        LevenshteinDistance levenshtein, JaroWinklerSimilarity jaroWinkler) {
    // Normalize strings
    String s1 = normalizeTitle(title1);
    String s2 = normalizeTitle(title2);

    // Calculate different similarity metrics
    double levenshteinSim = 1.0 - ((double) levenshtein.apply(s1, s2) / Math.max(s1.length(), s2.length()));
    double jaroWinklerSim = jaroWinkler.apply(s1, s2);

    // Combine scores (weighted average)
    return (levenshteinSim * 0.4) + (jaroWinklerSim * 0.6);
}

private String normalizeTitle(String title) {
    return title.toLowerCase()
            .replaceAll("\\(.*?\\)", "") // Remove content in parentheses
            .replaceAll("\\[.*?\\]", "") // Remove content in brackets
            .replaceAll("ft\\.|feat\\.", "") // Remove featuring artists
            .replaceAll("official.*video", "") // Remove "official video" etc.
            .replaceAll("\\s+", " ") // Normalize whitespace
            .trim();
}    private List<YoutubeResponse> sortResultsByRelevanceYT(List<YoutubeResponse> results, YoutubeSearchQuery query) {
        if (results == null || results.isEmpty()) {
            return results;
        }

        LevenshteinDistance levenshtein = new LevenshteinDistance();
        JaroWinklerSimilarity jaroWinkler = new JaroWinklerSimilarity();

        results.sort((a, b) -> {
            double similarityA = calculateVideoRelevanceScore(a, query, levenshtein, jaroWinkler);
            double similarityB = calculateVideoRelevanceScore(b, query, levenshtein, jaroWinkler);
            return Double.compare(similarityB, similarityA);
        });

        double threshold = 0.4;
        return results.stream()
                .filter(result -> calculateVideoRelevanceScore(result, query, levenshtein, jaroWinkler) >= threshold)
                .limit(5)
                .toList();
    }    private double calculateVideoRelevanceScore(YoutubeResponse video, YoutubeSearchQuery query,
            LevenshteinDistance levenshtein, JaroWinklerSimilarity jaroWinkler) {
        // Compare both title and artist name
        double titleSimilarity = calculateRelevanceScore(
            video.getSongTitle(),
            query.getTitle(),
            levenshtein,
            jaroWinkler
        );
        
        double artistSimilarity = calculateRelevanceScore(
            video.getArtistName(),
            query.getArtist(),
            levenshtein,
            jaroWinkler
        );

        // Weight title similarity higher than artist similarity
        return (titleSimilarity * 0.7) + (artistSimilarity * 0.3);
    }

    private double calculateSpotifyRelevanceScore(SpotifyResponse spotify, SpotifySearchQuery query,
            LevenshteinDistance levenshtein, JaroWinklerSimilarity jaroWinkler) {
        // Compare both title and artist name
        double titleSimilarity = calculateRelevanceScore(
            spotify.getSongTitle(),
            query.getTitle(),
            levenshtein,
            jaroWinkler
        );
        
        double artistSimilarity = calculateRelevanceScore(
            spotify.getArtistName(),
            query.getArtist(),
            levenshtein,
            jaroWinkler
        );

        // Weight title similarity higher than artist similarity
        return (titleSimilarity * 0.7) + (artistSimilarity * 0.3);
    }
}
