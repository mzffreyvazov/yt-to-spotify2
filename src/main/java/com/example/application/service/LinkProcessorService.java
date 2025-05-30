package com.example.application.service;

import org.springframework.stereotype.Service;

@Service
public class LinkProcessorService {
    
    private SpotifyService spotifyService;
    private YoutubeService youtubeService;
    private LinkConvertor linkConvertor;
    
    public LinkProcessorService(SpotifyService spotifyService, YoutubeService youtubeService, LinkConvertor linkConvertor) {
        this.spotifyService = spotifyService;
        this.youtubeService = youtubeService;
        this.linkConvertor = linkConvertor;
    }

}
