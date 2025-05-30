package com.example.application.config;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.reactive.function.client.WebClient;

@Configuration
public class ApiWebClientsConfig {
    
    @Value("${api.search.base-url}")
    private String searchBaseUrl;

    @Value("${api.track.base-url}")
    private String trackBaseUrl;

    @Bean
    @Qualifier("searchWebClient") // Use a qualifier for clarity
    public WebClient searchWebClient(WebClient.Builder webClientBuilder) {
        return webClientBuilder
                .baseUrl(searchBaseUrl)
                // You can add specific filters, timeouts, etc., for the search API
                .build();
    }

    @Bean
    @Qualifier("trackWebClient") // Another qualifier for the track API
    public WebClient trackWebClient(WebClient.Builder webClientBuilder) {
        return webClientBuilder
                .baseUrl(trackBaseUrl)
                // You can add specific filters, timeouts, etc., for the track API
                .build();
    }
}
