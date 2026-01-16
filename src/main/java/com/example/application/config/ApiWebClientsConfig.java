package com.example.application.config;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.reactive.function.client.WebClient;

@Configuration
public class ApiWebClientsConfig {

    private final ApiProperties apiProperties;

    public ApiWebClientsConfig(ApiProperties apiProperties) {
        this.apiProperties = apiProperties;
    }

    @Bean
    @Qualifier("searchWebClientSpotify") 
    public WebClient searchWebClient(WebClient.Builder webClientBuilder) {
        return webClientBuilder
                .baseUrl(apiProperties.getSpotify().getSearchBaseUrl())
                .build();
    }

    @Bean
    @Qualifier("trackWebClientSpotify") 
    public WebClient trackWebClient(WebClient.Builder webClientBuilder) {
        return webClientBuilder
                .baseUrl(apiProperties.getSpotify().getTrackBaseUrl())
                .build();
    }
    @Bean
    @Qualifier("searchWebClientYoutube") 
    public WebClient searchWebClientYt(WebClient.Builder webClientBuilder) {
        return webClientBuilder
                .baseUrl(apiProperties.getYoutube().getSearchBaseUrl())
                .build();
    }

    @Bean
    @Qualifier("trackWebClientYoutube") 
    public WebClient trackWebClientYt(WebClient.Builder webClientBuilder) {
        return webClientBuilder
                .baseUrl(apiProperties.getYoutube().getTrackBaseUrl())
                .build();
    }

}
