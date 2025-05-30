package com.example.application.model.youtube_dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ContentDetails {
    private String duration;
    private String dimension;
    private String definition;
    private String caption;
    private boolean licensedContent;
    private String projection;
}