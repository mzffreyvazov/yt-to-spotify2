package com.example.application.model.queries;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class SpotifySearchQuery {
    private String title;
    private String artist;
    private String album;
    
    /**
     * Creates a query string for Spotify search API using Spotify's advanced search syntax
     * Reference: Spotify uses format like "track:song_name artist:artist_name"
     * Field filters: track, artist, album, year, genre, isrc for track searches
     */
    public String toQueryString() {
        StringBuilder query = new StringBuilder();
        
        // Add track search filter if title is provided
        if (title != null && !title.isEmpty()) {
            String cleanTitle = cleanSearchTerm(title);
            query.append("track:").append(cleanTitle);
        }
        
        // Add artist search filter if artist is provided
        if (artist != null && !artist.isEmpty()) {
            if (!query.isEmpty()) query.append(" ");
            String cleanArtist = cleanSearchTerm(artist);
            query.append("artist:").append(cleanArtist);
        }
        
        // Add album search filter if album is provided
        if (album != null && !album.isEmpty()) {
            if (!query.isEmpty()) query.append(" ");
            String cleanAlbum = cleanSearchTerm(album);
            query.append("album:").append(cleanAlbum);
        }
        
        String result = query.toString();
        System.out.println("[Spotify Query Generator] toQueryString() generated: \"" + result + "\"");
        System.out.println("  - From Title: \"" + title + "\"");
        System.out.println("  - From Artist: \"" + artist + "\"");
        return result;
    }
    
    /**
     * Clean up search terms to improve search results
     * - Removes colons as they interfere with Spotify's search syntax
     * - Handles special characters that might affect search quality
     */
    private String cleanSearchTerm(String term) {
        if (term == null) return "";
        
        // Replace colons with spaces because they interfere with Spotify's search syntax
        String cleaned = term.replace(":", "");
        
        // Remove leading "The" for better matching
        if (cleaned.toLowerCase().startsWith("the ")) {
            cleaned = cleaned.substring(4);
        }
        
        // Handle featuring artists that could be in title
        cleaned = cleaned.replaceAll("(?i)\\sfeat\\.\\s.*", "").trim();
        cleaned = cleaned.replaceAll("(?i)\\sft\\.\\s.*", "").trim();
        
        // Other common cleanup for better search results
        cleaned = cleaned.replaceAll("\\s+", " ").trim(); // normalize whitespace
        
        return cleaned;
    }
    
    /**
     * Creates a general search query that can be used as a fallback
     * Does not use field-specific prefixes
     */
    public String toGeneralQueryString() {
        StringBuilder query = new StringBuilder();
        
        if (title != null && !title.isEmpty()) {
            query.append(cleanSearchTerm(title));
        }
        
        if (artist != null && !artist.isEmpty() && !title.contains(artist)) {
            if (query.length() > 0) query.append(" ");
            query.append(cleanSearchTerm(artist));
        }
        
        String result = query.toString();
        System.out.println("[Spotify Query Generator] toGeneralQueryString() generated: \"" + result + "\"");
        return result;
    }
}
