package com.example.application.model.youtube_dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class SearchItem {
    private String kind; 
    private String etag;
    private Id id; 
    private Snippet snippet;
}
