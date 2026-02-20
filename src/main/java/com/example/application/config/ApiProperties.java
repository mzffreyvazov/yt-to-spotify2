package com.example.application.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.NestedConfigurationProperty;

@ConfigurationProperties(prefix = "api")
public class ApiProperties {
    @NestedConfigurationProperty
    private final Spotify spotify = new Spotify();
    @NestedConfigurationProperty
    private final Youtube youtube = new Youtube();

    public Spotify getSpotify() {
        return spotify;
    }

    public Youtube getYoutube() {
        return youtube;
    }

    public static class Spotify {
        private String baseUrl;

        public String getBaseUrl() {
            return baseUrl;
        }

        public void setBaseUrl(String baseUrl) {
            this.baseUrl = baseUrl;
        }
    }

    public static class Youtube {
        private String baseUrl;

        public String getBaseUrl() {
            return baseUrl;
        }

        public void setBaseUrl(String baseUrl) {
            this.baseUrl = baseUrl;
        }
    }
}
