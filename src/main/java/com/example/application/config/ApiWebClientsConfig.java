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
    @Qualifier("searchWebClientSpotify") 
    public RestClient searchWebClient() {
        return RestClient.builder()
                .baseUrl(apiProperties.getSpotify().getSearchBaseUrl())
                .build();
    }

    @Bean
    @Qualifier("trackWebClientSpotify") 
    public RestClient trackWebClient() {
        return RestClient.builder()
                .baseUrl(apiProperties.getSpotify().getTrackBaseUrl())
                .build();
    }
    
    @Bean
    @Qualifier("searchWebClientYoutube") 
    public RestClient searchWebClientYt() {
        return RestClient.builder()
                .baseUrl(apiProperties.getYoutube().getSearchBaseUrl())
                .build();
    }

    @Bean
    @Qualifier("trackWebClientYoutube") 
    public RestClient trackWebClientYt() {
        return RestClient.builder()
                .baseUrl(apiProperties.getYoutube().getTrackBaseUrl())
                .build();
    }

}