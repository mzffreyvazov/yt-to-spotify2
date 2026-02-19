package com.example.application.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;

import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.example.application.model.queries.SpotifySearchQuery;
import com.example.application.model.response.SpotifyResponse;

@ExtendWith(MockitoExtension.class)
public class LInkProcessorServiceTest {

    @Mock
    private LinkConvertorService linkConvertorService;

    @Mock
    private SpotifyService spotifyService;

    @Mock
    private YoutubeService youtubeService;

    @InjectMocks
    private LinkProcessorService linkProcessorService;

    // Methods we are going to test:
    // public SpotifyResponse processYoutubeInput(String input)
    // public YoutubeResponse processSpotifyInput(String input)

    // public List<SpotifyResponse> processYoutubeLink(String youtubeUrl)
    // public List<YoutubeResponse> processSpotifyLink(String spotifyUrl)'
    // 

    // Test for processYoutubeLink
    // it needs: spotifySearchQuery

    private SpotifySearchQuery spotifyQuery;
    private List<SpotifyResponse> testResults;

    @BeforeEach
    public void setUp() {
        // Mockito will automatically inject mocks into linkProcessorServices
        spotifyQuery = new SpotifySearchQuery("Test Song", "Test Artist", null);
        testResults = List.of(new SpotifyResponse("TestId 123", "Test Title", "Test Artist", "image.url", null, "spotify.com/track/123"));
        
    }   

    @Test
    public void processYoutubeLink_whenFirstQueryReturnsResults_returnsImmediately() {
        when(linkConvertorService.youtubeToSpotifyQuery(anyString())).thenReturn(spotifyQuery);
        when(spotifyService.getSpotifyResponse(spotifyQuery.toQueryString())).thenReturn(testResults);

        List<SpotifyResponse> results = linkProcessorService.processYoutubeLink("https://www.youtube.com/watch?v=123");

        assertEquals(testResults, results);
    }
    
}
