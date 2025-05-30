package com.example.application.service;

import org.springframework.stereotype.Service;
import com.example.application.model.response.YoutubeResponse;
import com.example.application.model.queries.SpotifySearchQuery;
import reactor.core.publisher.Mono;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Service
public class LinkConvertorService {
    
    private final YoutubeService youtubeService;
    
    // Regex patterns for extracting IDs from different URL formats
    private static final Pattern YT_VIDEO_ID_PATTERN = Pattern.compile(
            "(?:youtube\\.com/watch\\?v=|youtu\\.be/)([\\w-]+)");
    private static final Pattern YT_MUSIC_ID_PATTERN = Pattern.compile(
            "music\\.youtube\\.com/watch\\?v=([\\w-]+)");
    private static final Pattern SPOTIFY_TRACK_ID_PATTERN = Pattern.compile(
            "open\\.spotify\\.com/track/([\\w\\d]+)");
    
    public LinkConvertorService(YoutubeService youtubeService) {
        this.youtubeService = youtubeService;
    }
    
    /**
     * Determines the type of link (YouTube or Spotify)
     */
    public String detectLinkType(String url) {
        if (url == null || url.isEmpty()) {
            return "INVALID";
        }
        
        if (url.contains("youtube.com") || url.contains("youtu.be")) {
            return "YOUTUBE";
        } else if (url.contains("spotify.com")) {
            return "SPOTIFY";
        } else {
            return "UNKNOWN";
        }
    }
    
    /**
     * Extracts video ID from a YouTube URL
     */
    public String extractYoutubeId(String youtubeUrl) {
        Matcher matcher = YT_VIDEO_ID_PATTERN.matcher(youtubeUrl);
        if (matcher.find()) {
            return matcher.group(1);
        }
        
        // Try YouTube Music pattern if standard pattern fails
        matcher = YT_MUSIC_ID_PATTERN.matcher(youtubeUrl);
        if (matcher.find()) {
            return matcher.group(1);
        }
        
        return null;
    }
    
    /**
     * Extracts track ID from a Spotify URL
     */
    public String extractSpotifyId(String spotifyUrl) {
        Matcher matcher = SPOTIFY_TRACK_ID_PATTERN.matcher(spotifyUrl);
        if (matcher.find()) {
            return matcher.group(1);
        }
        return null;
    }
    
    /**
     * Converts a YouTube link to a Spotify search query
     */
    public Mono<SpotifySearchQuery> youtubeToSpotifyQuery(String youtubeUrl) {
        String videoId = extractYoutubeId(youtubeUrl);
        if (videoId == null) {
            return Mono.error(new IllegalArgumentException("Invalid YouTube URL: " + youtubeUrl));
        }
        
        return youtubeService.getSingleVideo(videoId)
                .map(this::createSpotifyQueryFromYoutubeResponse);
    }
    
    /**
     * Creates a Spotify search query from YouTube response data
     */
    private SpotifySearchQuery createSpotifyQueryFromYoutubeResponse(YoutubeResponse ytResponse) {
        if (ytResponse == null) {
            return new SpotifySearchQuery();
        }
        
        // Clean up title and artist name to create an effective search query
        String songTitle = cleanupTitle(ytResponse.getSongTitle());
        String artistName = ytResponse.getArtistName();
        
        SpotifySearchQuery query = new SpotifySearchQuery();
        query.setTitle(songTitle);
        query.setArtist(artistName);
        
        return query;
    }
    
    /**
     * Cleans up the video title to extract just the song name
     * Removes common patterns like "Official Video", "ft.", etc.
     */
    private String cleanupTitle(String title) {
        if (title == null) return "";
        
        // Remove patterns like (Official Video), [Official Music Video], etc.
        String cleaned = title.replaceAll("(?i)\\(Official.*?\\)|\\[Official.*?\\]", "");
        
        // Remove patterns like ft. Artist, feat. Artist
        cleaned = cleaned.replaceAll("(?i)\\s*ft\\..*|\\s*feat\\..*", "");
        
        // Remove other common noise in YouTube video titles
        cleaned = cleaned.replaceAll("(?i)\\s*\\|.*", ""); // Remove anything after |
        cleaned = cleaned.replaceAll("(?i)\\s*\\(lyrics\\).*", ""); // Remove (lyrics)
        cleaned = cleaned.replaceAll("(?i)\\s*official\\s*music\\s*video.*", ""); // Remove "official music video"
        
        // Trim any leading/trailing whitespace
        return cleaned.trim();
    }
}
