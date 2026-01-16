package com.example.application.controller;

import java.util.List;

import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.example.application.model.response.YoutubeResponse;
import com.example.application.service.YoutubeService;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;


@RestController
@RequestMapping("/api/youtube")
public class YoutubeController {
    
    private YoutubeService youtubeService;
    
    public YoutubeController(YoutubeService youtubeService) {
        this.youtubeService = youtubeService;
    }

    @GetMapping("/search")
    public List<YoutubeResponse> searchVideos(@RequestParam String query) {

        return youtubeService.getYoutubeResponse(query);
    }

    @GetMapping("/video")
    public YoutubeResponse getVideo(@RequestParam String videoId) {
        return youtubeService.getSingleVideo(videoId);
    }
    
}
