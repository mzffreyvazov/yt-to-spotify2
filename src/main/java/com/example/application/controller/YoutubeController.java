package com.example.application.controller;

import java.util.List;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.example.application.model.response.YoutubeResponse;
import com.example.application.service.YoutubeService;

import reactor.core.publisher.Mono;

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
    public Mono<List<YoutubeResponse>> searchVideos(@RequestParam String query) {
        // The service now returns a Mono<List<YoutubeResponse>>
        // Spring WebFlux will automatically subscribe to this Mono
        // and return the List<YoutubeResponse> in the HTTP response once available.
        return youtubeService.getYoutubeResponse(query);
    }

    
}
