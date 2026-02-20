package com.example.application.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

import java.util.List;

import com.example.application.exception.InvalidLinkException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
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

    // for the first immediate results
    private SpotifySearchQuery spotifyQuery;
    private List<SpotifyResponse> testResults;

    // for the second fallback strategy
    private List<SpotifyResponse> emptyTestResults;
    private List<SpotifyResponse> generalTestResults;
    private List<SpotifyResponse> titleTestResults;


    @BeforeEach
    public void setUp() {
        // Mockito will automatically inject mocks into linkProcessorServices

        // for the first immediate results
        spotifyQuery = new SpotifySearchQuery("Test Song", "Test Artist", null);
        testResults = List.of(new SpotifyResponse("TestId 123", "Test Title", "Test Artist", "image.url", null, "spotify.com/track/123"));

        // for the second fallback strategy
        emptyTestResults = List.of();
        generalTestResults = List.of(new SpotifyResponse("General TestId 123", "General Test Title", "General Test Artist", "General image.url", null, "General spotify.com/track/123"));
        titleTestResults = List.of(new SpotifyResponse("Title TestId 123", "Title Test Title", "Title Test Artist", "Title image.url", null, "Title spotify.com/track/123"));

    }

    @Test
    @DisplayName("processYoutubeLink - First Query")
    public void processYoutubeLink_whenFirstQueryReturnsResults_returnsImmediately() {
        when(linkConvertorService.youtubeToSpotifyQuery(anyString())).thenReturn(spotifyQuery);
        when(spotifyService.getSpotifyResponse(spotifyQuery.toQueryString())).thenReturn(testResults);

        List<SpotifyResponse> results = linkProcessorService.processYoutubeLink("https://www.youtube.com/watch?v=123");

        assertEquals(testResults, results);
    }

    @Test
    @DisplayName("processYoutubeLink - Second Fallback: General Query")
    public void processYoutubelInk_whenFirstQueryNotReturnsResults_shouldFallbackGeneralQuery() {
        // 1. ARRANGE (Set all the rules)
        when(linkConvertorService.youtubeToSpotifyQuery(anyString())).thenReturn(spotifyQuery);

        // First call fails
        when(spotifyService.getSpotifyResponse(spotifyQuery.toQueryString())).thenReturn(emptyTestResults);
        // Second call succeeds
        when(spotifyService.getSpotifyResponse(spotifyQuery.toGeneralQueryString())).thenReturn(generalTestResults);


        // 2. ACT (Run the real code)
        List<SpotifyResponse> results = linkProcessorService.processYoutubeLink("https://www.youtube.com/watch?v=123");


        // 3. ASSERT (Check the results and verify the interactions)
        assertEquals(generalTestResults, results);

        // Verify it tried the specific query
        verify(spotifyService).getSpotifyResponse(spotifyQuery.toQueryString());

        // Verify it tried the general query
        verify(spotifyService).getSpotifyResponse(spotifyQuery.toGeneralQueryString());

        // Verify it NEVER tried the title-only query (because the general query succeeded)
        verify(spotifyService, never()).getSpotifyResponse("track:" + spotifyQuery.getTitle());

    }

    @Test
    @DisplayName("processYoutubeLink - Third Fallback: Title Only Query")
    public void processYoutubeLink_whenSecondFallbackNotReturns_shouldFallbackTitleOnlyQuery() {

        // 1. ARRANGE (Set all the rules)
        when(linkConvertorService.youtubeToSpotifyQuery(anyString())).thenReturn(spotifyQuery);

        // First call fails
        when(spotifyService.getSpotifyResponse(spotifyQuery.toQueryString())).thenReturn(emptyTestResults);

        // Second call should fail
        when(spotifyService.getSpotifyResponse(spotifyQuery.toGeneralQueryString())).thenReturn(emptyTestResults);

        // Third call should succeed
        when(spotifyService.getSpotifyResponse("track:" + spotifyQuery.getTitle())).thenReturn(titleTestResults);


        List<SpotifyResponse> results = linkProcessorService.processYoutubeLink("https://www.youtube.com/watch?v=123");

        assertEquals(titleTestResults, results);


        // Verify it tried the specific query
        verify(spotifyService).getSpotifyResponse(spotifyQuery.toQueryString());

        // Verify it tried the general query
        verify(spotifyService).getSpotifyResponse(spotifyQuery.toGeneralQueryString());

        // Verify it NEVER tried the title-only query (because the general query succeeded)
        verify(spotifyService).getSpotifyResponse("track:" + spotifyQuery.getTitle());

    }


    @Test
    @DisplayName("processYoutubeInput - SPOTIFY link shouold throw InvalidLinkException")
    public void processYoutubeInput_whenLinkTypeSpotify_shouldThrowInvalidLinkException() {

        // ARRANGE
        when(linkConvertorService.detectLinkType(anyString())).thenReturn("SPOTIFY");

        // ACT & ASSERT
        assertThrows(InvalidLinkException.class, () -> linkProcessorService.processYoutubeInput("spotify.com/track/123"));
    }


}
