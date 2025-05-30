package com.example.application.config;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.reactive.function.client.WebClient;

@Configuration
public class ApiWebClientsConfig {
    
    @Value("${api.search.base-url}")
    private String searchBaseUrlSpotify;

    @Value("${api.track.base-url}")
    private String trackBaseUrlSpotify;

    @Value("${api.search.base-url.youtube}")
    private String searchBaseUrlYoutube;

    @Value("${api.track.base-url.youtube}")
    private String trackBaseUrlYoutube;        

    @Bean
    @Qualifier("searchWebClientSpotify") // Use a qualifier for clarity
    public WebClient searchWebClient(WebClient.Builder webClientBuilder) {
        return webClientBuilder
                .baseUrl(searchBaseUrlSpotify)
                // You can add specific filters, timeouts, etc., for the search API
                .build();
    }

    @Bean
    @Qualifier("trackWebClientSpotify") // Another qualifier for the track API
    public WebClient trackWebClient(WebClient.Builder webClientBuilder) {
        return webClientBuilder
                .baseUrl(trackBaseUrlSpotify)
                // You can add specific filters, timeouts, etc., for the track API
                .build();
    }
    @Bean
    @Qualifier("searchWebClientYoutube") // Use a qualifier for clarity
    public WebClient searchWebClientYt(WebClient.Builder webClientBuilder) {
        return webClientBuilder
                .baseUrl(searchBaseUrlYoutube)
                // You can add specific filters, timeouts, etc., for the search API
                .build();
    }

    @Bean
    @Qualifier("trackWebClientYoutube") // Another qualifier for the track API
    public WebClient trackWebClientYt(WebClient.Builder webClientBuilder) {
        return webClientBuilder
                .baseUrl(trackBaseUrlYoutube)
                // You can add specific filters, timeouts, etc., for the track API
                .build();
    }

}
