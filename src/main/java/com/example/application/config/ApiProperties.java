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
        private String searchBaseUrl;
        private String trackBaseUrl;

        public String getSearchBaseUrl() {
            return searchBaseUrl;
        }

        public void setSearchBaseUrl(String searchBaseUrl) {
            this.searchBaseUrl = searchBaseUrl;
        }

        public String getTrackBaseUrl() {
            return trackBaseUrl;
        }

        public void setTrackBaseUrl(String trackBaseUrl) {
            this.trackBaseUrl = trackBaseUrl;
        }
    }

    public static class Youtube {
        private String searchBaseUrl;
        private String trackBaseUrl;

        public String getSearchBaseUrl() {
            return searchBaseUrl;
        }

        public void setSearchBaseUrl(String searchBaseUrl) {
            this.searchBaseUrl = searchBaseUrl;
        }

        public String getTrackBaseUrl() {
            return trackBaseUrl;
        }

        public void setTrackBaseUrl(String trackBaseUrl) {
            this.trackBaseUrl = trackBaseUrl;
        }
    }
}
