package com.example.application.model.queries;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor 
@AllArgsConstructor
public class YoutubeSearchQuery {
    private String title;
    private String artist;
    private String album;

    public String toQueryString() {
        StringBuilder query = new StringBuilder();

        // Add title search filter if title is provided
        if (title != null && !title.isEmpty()) {
            query.append("").append(cleanSearchTerm(title));
        }

        // Add artist search filter if artist is provided
        if (artist != null && !artist.isEmpty()) {
            if (query.length() > 0) query.append(" ");
            query.append("").append(cleanSearchTerm(artist));
        }

        // Add album search filter if album is provided
        if (album != null && !album.isEmpty()) {
            if (query.length() > 0) query.append(" ");
            query.append("").append(cleanSearchTerm(album));
        }

        return query.toString();
    }

    private String cleanSearchTerm(String term) {
        if (term == null) return "";

        // Replace colons with spaces to avoid issues with search syntax
        String cleaned = term.replace(":", "");

        // Remove leading "The" for better matching
        if (cleaned.toLowerCase().startsWith("the ")) {
            cleaned = cleaned.substring(4);
        }

        return cleaned.trim();
    }

    public String toGeneralQueryString() {
        StringBuilder query = new StringBuilder();

        // Add title search filter if title is provided
        if (title != null && !title.isEmpty()) {
            query.append("title:").append(cleanSearchTerm(title));
        }

        // Add artist search filter if artist is provided
        if (artist != null && !artist.isEmpty()) {
            if (query.length() > 0) query.append(" ");
            query.append("artist:").append(cleanSearchTerm(artist));
        }

        return query.toString();
    }
    
}
