package com.example.application.config;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.client.RestClient;

@Configuration
public class ApiWebClientsConfig {

    private final ApiProperties apiProperties;

    public ApiWebClientsConfig(ApiProperties apiProperties) {
        this.apiProperties = apiProperties;
    }

    @Bean
    @Qualifier("spotifyClient")
    public RestClient spotifyClient() {
        return RestClient.builder()
                .baseUrl(apiProperties.getSpotify().getBaseUrl())
                .build();
    }

    @Bean
    @Qualifier("youtubeClient")
    public RestClient youtubeClient() {
        return RestClient.builder()
                .baseUrl(apiProperties.getYoutube().getBaseUrl())
                .build();
    }

}