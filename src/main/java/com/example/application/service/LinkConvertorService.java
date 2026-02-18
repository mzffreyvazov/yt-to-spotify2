package com.example.application.service;

import org.springframework.stereotype.Service;

import com.example.application.model.response.SpotifyResponse;
import com.example.application.model.response.YoutubeResponse;
import com.example.application.exception.InvalidLinkException;
import com.example.application.model.queries.SpotifySearchQuery;
import com.example.application.model.queries.YoutubeSearchQuery;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Service
public class LinkConvertorService {
    
    private final YoutubeService youtubeService;
    private final SpotifyService spotifyService;
    
    // Regex patterns for extracting IDs from different URL formats
    private static final Pattern YT_VIDEO_ID_PATTERN = Pattern.compile(
            "(?:youtube\\.com/watch\\?v=|youtu\\.be/)([\\w-]+)");
    private static final Pattern YT_MUSIC_ID_PATTERN = Pattern.compile(
            "music\\.youtube\\.com/watch\\?v=([\\w-]+)");
    private static final Pattern SPOTIFY_TRACK_ID_PATTERN = Pattern.compile(
            "open\\.spotify\\.com/track/([\\w\\d]+)");
    
    public LinkConvertorService(YoutubeService youtubeService, SpotifyService spotifyService) {
        this.spotifyService = spotifyService;
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
    public SpotifySearchQuery youtubeToSpotifyQuery(String youtubeUrl) {
        String videoId = extractYoutubeId(youtubeUrl);
        if (videoId == null) {
            throw new InvalidLinkException("Invalid YouTube URL: " + youtubeUrl);
        }
        
        YoutubeResponse ytResponse = youtubeService.getSingleVideo(videoId);
        return createSpotifyQueryFromYoutubeResponse(ytResponse);
    }

    public YoutubeSearchQuery spotifyToYoutubeQuery(String spotifyUrl) {
        String trackId = extractSpotifyId(spotifyUrl);
        if (trackId == null) {
            throw new InvalidLinkException("Invalid Spotify URL: " + spotifyUrl);
        }
        
        // Here you would implement logic to convert Spotify track ID to a YouTube search query
        // This is a placeholder as the actual implementation depends on your requirements
        SpotifyResponse spotifyResponse = this.spotifyService.getSingleTrack(trackId);
        return createYoutubeSearchQueryFromSpotify(spotifyResponse);
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
        String artistName = cleanupArtist(ytResponse.getArtistName());
        
        System.out.println("\n========== YOUTUBE TO SPOTIFY CONVERSION ==========");
        System.out.println("YouTube video detected:");
        System.out.println("  Original Title: " + ytResponse.getSongTitle());
        System.out.println("  Original Artist: " + ytResponse.getArtistName());
        System.out.println("  Cleaned Title: " + songTitle);
        System.out.println("  Cleaned Artist: " + artistName);
        System.out.println("====================================================\n");
        
        SpotifySearchQuery query = new SpotifySearchQuery();
        query.setTitle(songTitle);
        query.setArtist(artistName);
        
        return query;
    }

    public YoutubeSearchQuery createYoutubeSearchQueryFromSpotify(SpotifyResponse spotifyResponse) {
        if (spotifyResponse == null) {
            return new YoutubeSearchQuery();
        }
        
        // Clean up title and artist name to create an effective search query
        String songTitle = cleanupTitle(spotifyResponse.getSongTitle());
        String artistName = spotifyResponse.getArtistName();
        
        System.out.println("\n========== SPOTIFY TO YOUTUBE CONVERSION ==========");
        System.out.println("Spotify track detected:");
        System.out.println("  Track Title: " + spotifyResponse.getSongTitle());
        System.out.println("  Artist: " + spotifyResponse.getArtistName());
        System.out.println("  Spotify URL: " + spotifyResponse.getSpotifyUrl());
        System.out.println("  Cleaned Title: " + songTitle);
        System.out.println("====================================================\n");
        
        YoutubeSearchQuery query = new YoutubeSearchQuery();
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
    
    private String cleanupArtist(String artist) {
        if (artist == null) return "";
        
        // Remove common noise in artist names
        String cleaned = artist.replaceAll("(?i)\\s*\\(.*?\\)", ""); // Remove anything in parentheses
        cleaned = cleaned.replaceAll("(?i)\\s*\\[.*?\\]", ""); // Remove anything in brackets
        cleaned = cleaned.replaceAll("(?i)\\s*ft\\..*", ""); // Remove featuring artists
        cleaned = cleaned.replaceAll("(?i)\\s*-\\s*Topic\\s*$", ""); // Remove "- Topic" suffix
        cleaned = cleaned.replaceAll("(?i)\\s*VEVO\\s*$", ""); // Remove "VEVO" suffix
        cleaned = cleaned.replaceAll("(?i)\\s*Official\\s*$", ""); // Remove "Official" suffix
        cleaned = cleaned.replaceAll("(?i)\\s*Music\\s*$", ""); // Remove "Music" suffix
        
        return cleaned.trim();
    }
}
