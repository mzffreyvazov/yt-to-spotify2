package com.example.application.model.youtube_dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class Thumbnail {
    private String url;
    private int width;
    private int height;
}