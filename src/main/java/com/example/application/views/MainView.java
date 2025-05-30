package com.example.application.views;

import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.html.H1;
import com.vaadin.flow.component.html.Paragraph;
import com.vaadin.flow.component.icon.Icon;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;

@PageTitle("Convert Music Links")
@Route(value = "") // Set as the default route
public class MainView extends VerticalLayout {

    private TextField linkInput;
    private Button searchButton;
    private VerticalLayout resultsLayout;
    private Button youtubeToSpotifyButton;
    private Button spotifyToYoutubeButton;

    private boolean youtubeToSpotifyMode = true; // true for YouTube to Spotify, false for Spotify to YouTube

    public MainView() {
        setSizeFull();
        setAlignItems(Alignment.CENTER);
        getStyle().set("padding-top", "5%"); // Add some padding at the top

        H1 title = new H1("Convert Music Links");
        Paragraph description = new Paragraph("Transform your favorite music between YouTube and Spotify with a single click. Just paste the link and we\'ll find the closest matches.");
        description.getStyle().set("text-align", "center");
        description.getStyle().set("max-width", "600px");

        youtubeToSpotifyButton = new Button("YouTube to Spotify", new Icon(VaadinIcon.YOUTUBE));
        spotifyToYoutubeButton = new Button("Spotify to YouTube", new Icon(VaadinIcon.YOUTUBE));

        HorizontalLayout modeButtonsLayout = new HorizontalLayout(youtubeToSpotifyButton, spotifyToYoutubeButton);
        modeButtonsLayout.setSpacing(true);

        linkInput = new TextField();
        linkInput.setWidth("clamp(300px, 50%, 600px)"); // Responsive width
        linkInput.setClearButtonVisible(true);

        searchButton = new Button("", new Icon(VaadinIcon.SEARCH)); // Text will be set by updateUIMode
        searchButton.addThemeVariants(ButtonVariant.LUMO_PRIMARY, ButtonVariant.LUMO_SUCCESS); // Green button
        searchButton.setWidth("clamp(300px, 50%, 600px)");

        // Event listeners for mode buttons
        youtubeToSpotifyButton.addClickListener(event -> {
            if (!youtubeToSpotifyMode) {
                youtubeToSpotifyMode = true;
                updateUIMode();
            }
        });

        spotifyToYoutubeButton.addClickListener(event -> {
            if (youtubeToSpotifyMode) {
                youtubeToSpotifyMode = false;
                updateUIMode();
            }
        });
        
        updateUIMode(); // Call to set initial UI state

        searchButton.addClickListener(event -> {
            String link = linkInput.getValue();
            if (link != null && !link.trim().isEmpty()) {
                processLink(link);
            }
        });

        resultsLayout = new VerticalLayout();
        resultsLayout.setWidth("clamp(300px, 50%, 600px)");
        resultsLayout.setAlignItems(Alignment.STRETCH); // Results should stretch

        add(title, description, modeButtonsLayout, linkInput, searchButton, resultsLayout);
    }
    
    private void updateUIMode() {
        if (youtubeToSpotifyMode) {
            youtubeToSpotifyButton.removeThemeVariants(ButtonVariant.LUMO_TERTIARY);
            youtubeToSpotifyButton.addThemeVariants(ButtonVariant.LUMO_PRIMARY);
            // To achieve the red color from the image, you might use:
            // youtubeToSpotifyButton.getStyle().set("background-color", "var(--lumo-error-color)");
            // youtubeToSpotifyButton.getStyle().set("color", "var(--lumo-error-contrast-color)");


            spotifyToYoutubeButton.removeThemeVariants(ButtonVariant.LUMO_PRIMARY);
            // spotifyToYoutubeButton.getStyle().remove("background-color");
            // spotifyToYoutubeButton.getStyle().remove("color");
            spotifyToYoutubeButton.addThemeVariants(ButtonVariant.LUMO_TERTIARY);

            linkInput.setPlaceholder("Paste YouTube link here...");
            linkInput.setSuffixComponent(new Icon(VaadinIcon.YOUTUBE));
            searchButton.setText("Find on Spotify");
        } else {
            spotifyToYoutubeButton.removeThemeVariants(ButtonVariant.LUMO_TERTIARY);
            spotifyToYoutubeButton.addThemeVariants(ButtonVariant.LUMO_PRIMARY);

            youtubeToSpotifyButton.removeThemeVariants(ButtonVariant.LUMO_PRIMARY);
            // youtubeToSpotifyButton.getStyle().remove("background-color");
            // youtubeToSpotifyButton.getStyle().remove("color");
            youtubeToSpotifyButton.addThemeVariants(ButtonVariant.LUMO_TERTIARY);

            linkInput.setPlaceholder("Paste Spotify link here...");
            linkInput.setSuffixComponent(new Icon(VaadinIcon.YOUTUBE));
            searchButton.setText("Find on YouTube");
        }
    }

    private void processLink(String link) {
        resultsLayout.removeAll(); // Clear previous results
        resultsLayout.add(new Paragraph("Processing link: " + link));

        if (youtubeToSpotifyMode) {
            resultsLayout.add(new Paragraph("Mode: YouTube to Spotify"));
            // TODO: Implement YouTube to Spotify logic
            // 1. Validate/Parse YouTube link (e.g., extract video ID)
            // 2. Call a new YouTubeService to get video details (title, artist) using the video ID.
            //    - This service will likely use the YouTube Data API v3.
            //    - Requires an API key.
            // 3. Call a new SpotifyService to search for tracks using the extracted title and artist.
            //    - This service will use the Spotify Search API.
            //    - Requires Spotify API credentials (Client ID, Client Secret for Client Credentials Flow).
            // 4. Display up to 10 Spotify results in resultsLayout. Each result could be a small card with
            //    song title, artist, album, and a link to the song on Spotify.
            resultsLayout.add(new Paragraph("Next steps: Implement YouTube metadata extraction and Spotify search."));
            resultsLayout.add(new Paragraph("Example (dummy data):"));
            for (int i = 1; i <= 3; i++) {
                 resultsLayout.add(new Paragraph("Spotify Match " + i + ": Song Title by Artist - open.spotify.com/track/example" + i));
            }

        } else {
            resultsLayout.add(new Paragraph("Mode: Spotify to YouTube"));
            // TODO: Implement Spotify to YouTube logic
            // 1. Validate/Parse Spotify link (e.g., extract track ID)
            // 2. Call SpotifyService to get track details (song name, artist) using the track ID.
            // 3. Call YouTubeService (or directly use YouTube Data API) to search for music videos
            //    using the song name and artist.
            // 4. Display YouTube video results.
            resultsLayout.add(new Paragraph("Logic for Spotify to YouTube not yet implemented."));
        }
    }
}
