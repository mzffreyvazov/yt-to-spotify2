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
    @Qualifier("searchWebClientSpotify") 
    public WebClient searchWebClient(WebClient.Builder webClientBuilder) {
        return webClientBuilder
                .baseUrl(searchBaseUrlSpotify)
                .build();
    }

    @Bean
    @Qualifier("trackWebClientSpotify") 
    public WebClient trackWebClient(WebClient.Builder webClientBuilder) {
        return webClientBuilder
                .baseUrl(trackBaseUrlSpotify)
                .build();
    }
    @Bean
    @Qualifier("searchWebClientYoutube") 
    public WebClient searchWebClientYt(WebClient.Builder webClientBuilder) {
        return webClientBuilder
                .baseUrl(searchBaseUrlYoutube)
                .build();
    }

    @Bean
    @Qualifier("trackWebClientYoutube") 
    public WebClient trackWebClientYt(WebClient.Builder webClientBuilder) {
        return webClientBuilder
                .baseUrl(trackBaseUrlYoutube)
                .build();
    }

}
