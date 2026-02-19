package com.example.application.service;

import static org.junit.jupiter.api.Assertions.assertEquals; // ← JUnit 5
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.util.stream.Stream;

import org.junit.jupiter.api.*;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.MethodSource;
import org.junit.jupiter.params.provider.NullAndEmptySource;
//import org.junit.jupiter.params.provider.NullSource;
import org.junit.jupiter.params.provider.ValueSource;
import com.example.application.exception.InvalidLinkException;


@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class LinkConverterServiceTest {
    
    private LinkConvertorService linkConvertorService;


    // Pass null for dependencies since we won't be testing them here
    // I'm only testing the link parsing logic, so the actual service calls are not needed
    @BeforeEach
    public void setUp() {
        linkConvertorService = new LinkConvertorService(null, null); 
    }

    // Methods we have (without dependency calls): 
    // String detectLinkType(String url) 
    // String extractYoutubeId(String youtubeUrl)
    // String extractSpotifyId(String spotifyUrl)
    // String cleanupTitle(String title)
    // String cleanupArtist(String artist)

    // Writing tests (parameterized) for detectLinkType method
    @Order(1)
    @ParameterizedTest
    @DisplayName("Detect Link Type - YouTube URLs")
    @ValueSource(strings = {
        "https://www.youtube.com/watch?v=dQw4w9WgXcQ",
        "https://youtu.be/dQw4w9WgXcQ",
        "https://music.youtube.com/watch?v=dQw4w9WgXcQ"
    })
    public void detectLinkType_withYouTubeUrls_returnsYouTube(String url) {
        assertEquals("YOUTUBE", linkConvertorService.detectLinkType(url));
    }

    @Order(2)
    @ParameterizedTest
    @DisplayName("Detect Link Type - Spotify URLs")
    @ValueSource(strings = {
        "https://open.spotify.com/track/4567890",
        "https://open.spotify.com/playlist/1234567"
    })
    public void detectLinkType_withSpotifyUrls_returnsSpotify(String url) {
        assertEquals("SPOTIFY", linkConvertorService.detectLinkType(url));
    }

    @Order(3)
    @ParameterizedTest
    @DisplayName("Detect Link Type - Invalid URLs")
    @NullAndEmptySource
    public void detectLinkType_withInvalidUrls_returnsUnknown(String url) {
        assertEquals("INVALID", linkConvertorService.detectLinkType(url));
    }

    @Order(4)
    @ParameterizedTest
    @DisplayName("Detect Link Type - Unknown URLs")
    @ValueSource(strings = {
        "https://www.example.com",
        "https://twitter.com/someuser/status/1234567890"
    })
    public void detectLinkType_withUnknownUrls_returnsUnknown(String url) {
        assertEquals("UNKNOWN", linkConvertorService.detectLinkType(url));
    }


    // Writing tests (nested) for detectLinkType method
    @Nested
    @DisplayName("Detect Link Type (Nested) - YouTube URLs")
    class DetectLinkTypeTests {

        @Order(1)
        @ParameterizedTest
        @DisplayName("Detect Link Type - YouTube URLs")
        @ValueSource(strings = {
            "https://www.youtube.com/watch?v=dQw4w9WgXcQ",
            "https://youtu.be/dQw4w9WgXcQ",
            "https://music.youtube.com/watch?v=dQw4w9WgXcQ"
        })
        public void detectLinkType_withYouTubeUrls_returnsYouTube(String url) {
            assertEquals("YOUTUBE", linkConvertorService.detectLinkType(url));
        }
    }

    @Nested
    @DisplayName("Extract Youtube ID Tests")
    class ExtractYoutubeIdTests {

        @Order(1)
        @ParameterizedTest
        @CsvSource(ignoreLeadingAndTrailingWhitespace = true, value = {
            "https://www.youtube.com/watch?v=dQw4w9WgXcQ, dQw4w9WgXcQ",
            "https://youtu.be/dQw4w9WgXcQ,                dQw4w9WgXcQ",
            "https://music.youtube.com/watch?v=dQw4w9WgXcQ, dQw4w9WgXcQ",
            "https://music.youtube.com/watch?v=tHBfwoab49,  tHBfwoab49"
        })
        public void extractYoutubeId_withValidUrls_returnsId(String url, String expectedId) {
            assertEquals(expectedId, linkConvertorService.extractYoutubeId(url));
        }
    }   


    @Nested
    @DisplayName("Cleanup Title Tests")
    class CleanupTitleTests {

        @Order(1)
        @ParameterizedTest
        @CsvSource({
            "Shape of You (Official Video),                          Shape of You",
            "Blinding Lights [Official Music Video],                 Blinding Lights",
            "Rockstar ft. 21 Savage,                                 Rockstar",
            "Someone Like You feat. Adele,                           Someone Like You",
            "Bad Guy | Billie Eilish,                                Bad Guy",
            "Happier (Lyrics),                                       Happier",
            "Levitating Official Music Video,                        Levitating",
            "Stay,                                                   Stay"
        })
        public void cleanupTitle_withVariousTitles_returnsCleanTitle(String input, String expected) {
            assertEquals(expected, linkConvertorService.cleanupTitle(input));
        }

        @Order(2)
        @ParameterizedTest
        @NullAndEmptySource
        public void cleanupTitle_withNullOrEmpty_returnsEmpty(String input) {
            assertEquals("", linkConvertorService.cleanupTitle(input));
        }
    }

    @Nested
    @DisplayName("Cleanup Artist Tests")
    class CleanupArtistTests {

        @Order(1)
        @ParameterizedTest
        @CsvSource({
            "Ed Sheeran (Official),        Ed Sheeran",
            "Drake [Explicit],             Drake",
            "Eminem ft. Rihanna,           Eminem",
            "Lofi Girl - Topic,            Lofi Girl",
            "JustinBieberVEVO,             JustinBieber",
            "Adele Official,               Adele",
            "Universal Music,              Universal",
            "The Weeknd,                   The Weeknd"
        })
        public void cleanupArtist_withVariousArtists_returnsCleanArtist(String input, String expected) {
            assertEquals(expected, linkConvertorService.cleanupArtist(input));
        }

        @Order(2)
        @ParameterizedTest
        @NullAndEmptySource
        public void cleanupArtist_withNullOrEmpty_returnsEmpty(String input) {
            assertEquals("", linkConvertorService.cleanupArtist(input));
        }
    }

    // Writing tests (method source) for extractSpotifyId method
    @Order(5)
    @ParameterizedTest
    @MethodSource("provideSpotifyUrlsWithIds")
    @DisplayName("Extract Spotify ID (Method Source) - Valid URLs")
    public void extractSpotifyId_withValidUrls_returnsId(String url, String expectedId) {
        assertEquals(expectedId, linkConvertorService.extractSpotifyId(url));
    }

    private static Stream<Arguments> provideSpotifyUrlsWithIds() {
        return Stream.of(
            Arguments.of("https://open.spotify.com/track/4567890", "4567890"),
            Arguments.of("https://open.spotify.com/playlist/1234567", null), // Not a track URL
            Arguments.of("https://open.spotify.com/track/abcde12345", "abcde12345")
        );
    }


    @Order(6)
    @Test
    @DisplayName("Youtube to Spotify Query - Invalid URL Throws InvalidLinkException")
    public void youtubeSpotifyQuery_withNull_shouldThrowInvalidLinkException() {
        String badUrl = "https://google.com/search?q=hello";
        assertThrows(InvalidLinkException.class, () -> linkConvertorService.youtubeToSpotifyQuery(badUrl));
    }

    @Test
    @DisplayName("Spotify to YouTube Query - Invalid URL Throws InvalidLinkException")
    public void spotifyYoutubeQuery_withNull_shouldThrowInvalidLinkException() {
        String badUrl = "https://google.com/search?q=hello";
        assertThrows(InvalidLinkException.class, () -> linkConvertorService.spotifyToYoutubeQuery(badUrl));
    }
}
