package com.example.application.controller;

import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.example.application.model.response.SpotifyResponse;
import com.example.application.service.SpotifyService;

import reactor.core.publisher.Mono;

import java.util.List;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;


@RestController
@RequestMapping("/api/spotify")
public class SpotifyController {

    private SpotifyService spotifyService;
    public SpotifyController(SpotifyService spotifyService) {
        this.spotifyService = spotifyService;
    }

    @GetMapping("/search")
    public Mono<List<SpotifyResponse>> getMethodName(@RequestParam String query) {
        return spotifyService.getSpotifyResponse(query); 
    }
    
    @GetMapping("/search/track")
    public Mono<SpotifyResponse> getTrack(@RequestParam String trackId) {
        return spotifyService.getSingleTrack(trackId); 
    }
    
}
