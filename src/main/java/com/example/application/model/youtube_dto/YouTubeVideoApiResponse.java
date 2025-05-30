package com.example.application.model.youtube_dto;

import java.util.List;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class YouTubeVideoApiResponse {
    private String kind;
    private String etag;
    private List<VideoItem> items;
    private PageInfo pageInfo;
}