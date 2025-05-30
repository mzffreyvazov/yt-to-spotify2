package com.example.application.model.youtube_dto;

import com.fasterxml.jackson.annotation.JsonProperty;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class Thumbnails {
    
    
    @JsonProperty("default")
    private Thumbnail defaultThumbnail; 
    private Thumbnail medium;
    private Thumbnail high;
    private Thumbnail standard; 
    private Thumbnail maxres;   
}