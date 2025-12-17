package com.example.application.ui.views;

import com.example.application.ui.components.SongCard;
import com.example.application.model.response.SpotifyResponse;
import com.example.application.model.response.YoutubeResponse;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.html.H1;
import com.vaadin.flow.component.html.Paragraph;
import com.vaadin.flow.component.icon.Icon;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.notification.NotificationVariant;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.component.AttachEvent;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.MediaType;
import org.springframework.web.reactive.function.client.WebClient;
import java.util.List;


@PageTitle("Convert Music Links")
@Route(value = "") // Set as the default route
public class MainView extends VerticalLayout {

    private TextField linkInput;
    private Button searchButton;
    private VerticalLayout resultsLayout;
    private Button youtubeToSpotifyButton;
    private Button spotifyToYoutubeButton;
    private WebClient webClient;

    private boolean youtubeToSpotifyMode = true; // true for YouTube to Spotify, false for Spotify to YouTube

    public MainView() {
        String baseUrl = System.getProperty("app.base.url", "http://localhost:8080");

        // Initialize WebClient to call your REST endpoints
        this.webClient = WebClient.builder()
            .baseUrl(baseUrl) // Adjust port if different
            .build();
            
        setSizeFull();
        setAlignItems(Alignment.CENTER);
        getStyle().set("padding-top", "5%"); // Add some padding at the top

        H1 title = new H1("Convert Music Links");
        Paragraph description = new Paragraph("Transform your favorite music between YouTube and Spotify with a single click. Just paste the link and we'll find the closest matches.");
        description.getStyle().set("text-align", "center");
        description.getStyle().set("max-width", "600px");

        youtubeToSpotifyButton = new Button("YouTube to Spotify", new Icon(VaadinIcon.MUSIC));
        spotifyToYoutubeButton = new Button("Spotify to YouTube", new Icon(VaadinIcon.MUSIC));

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
            } else {
                showNotification("Please enter a valid link", NotificationVariant.LUMO_ERROR);
            }
        });

        linkInput.addKeyPressListener(com.vaadin.flow.component.Key.ENTER, event -> {
            String link = linkInput.getValue();
            if (link != null && !link.trim().isEmpty()) {
                processLink(link);
            }
        });

        resultsLayout = new VerticalLayout();
        resultsLayout.setWidth("clamp(300px, 80%, 800px)");
        resultsLayout.setAlignItems(Alignment.STRETCH); 

        add(title, description, modeButtonsLayout, linkInput, searchButton, resultsLayout);
    }
    
    @Override
    protected void onAttach(AttachEvent attachEvent) {
        super.onAttach(attachEvent);
        // Show first-time notification after component is attached to UI
        showFirstTimeNotification();
    }
    
    private void updateUIMode() {
        if (youtubeToSpotifyMode) {
            youtubeToSpotifyButton.removeThemeVariants(ButtonVariant.LUMO_TERTIARY);
            youtubeToSpotifyButton.addThemeVariants(ButtonVariant.LUMO_PRIMARY);

            spotifyToYoutubeButton.removeThemeVariants(ButtonVariant.LUMO_PRIMARY);
            spotifyToYoutubeButton.addThemeVariants(ButtonVariant.LUMO_TERTIARY);

            linkInput.setPlaceholder("Paste YouTube link here...");
            linkInput.setSuffixComponent(new Icon(VaadinIcon.MUSIC));
            searchButton.setText("Find on Spotify");
        } else {
            spotifyToYoutubeButton.removeThemeVariants(ButtonVariant.LUMO_TERTIARY);
            spotifyToYoutubeButton.addThemeVariants(ButtonVariant.LUMO_PRIMARY);

            youtubeToSpotifyButton.removeThemeVariants(ButtonVariant.LUMO_PRIMARY);
            youtubeToSpotifyButton.addThemeVariants(ButtonVariant.LUMO_TERTIARY);

            linkInput.setPlaceholder("Paste Spotify link here...");
            linkInput.setSuffixComponent(new Icon(VaadinIcon.MUSIC));
            searchButton.setText("Find on YouTube");
        }
    }

    private void processLink(String link) {
        resultsLayout.removeAll(); 
        
        searchButton.setEnabled(false);
        searchButton.setText("Searching...");
        
        Paragraph loadingMessage = new Paragraph("🔍 Searching for matches...");
        loadingMessage.getStyle().set("text-align", "center");
        resultsLayout.add(loadingMessage);

        if (youtubeToSpotifyMode) {
            searchYouTubeToSpotify(link);
        } else {
            searchSpotifyToYouTube(link);
        }
    }

    private void searchYouTubeToSpotify(String youtubeUrl) {
        webClient.get()
            .uri(uriBuilder -> uriBuilder
                .path("/api/links/youtube-to-spotify-tracks")
                .queryParam("youtubeUrl", youtubeUrl)
                .build())
            .accept(MediaType.APPLICATION_JSON)
            .retrieve()
            .bodyToMono(new ParameterizedTypeReference<List<SpotifyResponse>>() {})
            .subscribe(
                this::displaySpotifyResults,
                this::handleError
            );
    }

    private void searchSpotifyToYouTube(String spotifyUrl) {
        webClient.get()
            .uri(uriBuilder -> uriBuilder
                .path("/api/links/spotify-to-youtube")
                .queryParam("spotifyUrl", spotifyUrl)
                .build())
            .accept(MediaType.APPLICATION_JSON)
            .retrieve()
            .bodyToMono(new ParameterizedTypeReference<List<YoutubeResponse>>() {})
            .subscribe(
                this::displayYoutubeResults,
                this::handleError
            );
    }

    private void displaySpotifyResults(List<SpotifyResponse> spotifyTracks) {
        getUI().ifPresent(ui -> ui.access(() -> {
            // Detach resultsLayout before modifying its children
            this.remove(resultsLayout);
            
            resultsLayout.removeAll(); // Clear previous content
            
            if (spotifyTracks == null || spotifyTracks.isEmpty()) {
                Paragraph noResults = new Paragraph("No matching tracks found. Try a different link or check if the video is a music track.");
                noResults.getStyle().set("text-align", "center");
                noResults.getStyle().set("color", "var(--lumo-secondary-text-color)");
                resultsLayout.add(noResults);
            } else {
                Paragraph resultsHeader = new Paragraph("Found " + spotifyTracks.size() + " matching track" + (spotifyTracks.size() == 1 ? "" : "s") + ":");
                resultsHeader.getStyle().set("font-weight", "bold");
                resultsHeader.getStyle().set("margin-bottom", "var(--lumo-space-m)");
                resultsLayout.add(resultsHeader);
                
                // Create and add SongCard components for each result
                for (SpotifyResponse track : spotifyTracks) {
                    SongCard songCard = new SongCard(track);
                
                    
                    resultsLayout.add(songCard);
                }
                
                showNotification("Found " + spotifyTracks.size() + " matching tracks!", NotificationVariant.LUMO_SUCCESS);
            }
            
            // Re-attach resultsLayout after modifications
            this.add(resultsLayout);
            resetSearchButton();
        }));
    }

    private void displayYoutubeResults(List<YoutubeResponse> youtubeVideos) {
        getUI().ifPresent(ui -> ui.access(() -> {
            // Detach resultsLayout before modifying its children
            this.remove(resultsLayout);
            
            resultsLayout.removeAll(); // Clear previous content
            
            if (youtubeVideos == null || youtubeVideos.isEmpty()) {
                Paragraph noResults = new Paragraph("No matching videos found. Try a different link or check if the video is a music video.");
                noResults.getStyle().set("text-align", "center");
                noResults.getStyle().set("color", "var(--lumo-secondary-text-color)");
                resultsLayout.add(noResults);
            } else {
                Paragraph resultsHeader = new Paragraph("Found " + youtubeVideos.size() + " matching video" + (youtubeVideos.size() == 1 ? "" : "s") + ":");
                resultsHeader.getStyle().set("font-weight", "bold");
                resultsHeader.getStyle().set("margin-bottom", "var(--lumo-space-m)");
                resultsLayout.add(resultsHeader);
                
                // Create and add SongCard components for each result
                for (YoutubeResponse video : youtubeVideos) {
                    SongCard songCard = new SongCard(video);
                    resultsLayout.add(songCard);
                }
                
                showNotification("Found " + youtubeVideos.size() + " matching videos!", NotificationVariant.LUMO_SUCCESS);
            }
            
            // Re-attach resultsLayout after modifications
            this.add(resultsLayout);
            resetSearchButton();
        }));
    }

    private void handleError(Throwable error) {
        getUI().ifPresent(ui -> ui.access(() -> {
            // Detach resultsLayout before modifying its children
            this.remove(resultsLayout);

            resultsLayout.removeAll(); // Clear previous content
            if (youtubeToSpotifyMode) {
                Paragraph errorMessage = new Paragraph("❌ Error processing the link. Please check if it's a valid YouTube URL and try again.");
                errorMessage.getStyle().set("color", "var(--lumo-error-text-color)");
                errorMessage.getStyle().set("text-align", "center");
                resultsLayout.add(errorMessage);
            } else {
                Paragraph errorMessage = new Paragraph("❌ Error processing the link. Please check if it's a valid Spotify URL and try again.");
                errorMessage.getStyle().set("color", "var(--lumo-error-text-color)");
                errorMessage.getStyle().set("text-align", "center");
                resultsLayout.add(errorMessage);
            }
            
            
            // Log the error for debugging
            System.err.println("Error processing link: " + error.getMessage());
            error.printStackTrace();
            
            // Re-attach resultsLayout after modifications
            this.add(resultsLayout);
            showNotification("Error processing link: " + error.getMessage(), NotificationVariant.LUMO_ERROR);
            resetSearchButton();
        }));
    }

    private void resetSearchButton() {
        searchButton.setEnabled(true);
        if (youtubeToSpotifyMode) {
            searchButton.setText("Find on Spotify");
        } else {
            searchButton.setText("Find on YouTube");
        }
    }

    private void showNotification(String message, NotificationVariant variant) {
        Notification notification = Notification.show(message, 2000, Notification.Position.TOP_CENTER);
        notification.addThemeVariants(variant);
    }
    
    private void showFirstTimeNotification() {
        getUI().ifPresent(ui -> {
            // Check if the notification has been shown before using localStorage
            ui.getPage().executeJs(
                "return localStorage.getItem('firstTimeNotificationShown') === 'true';"
            ).then(Boolean.class, hasBeenShown -> {
                if (!hasBeenShown) {
                    // Create notification content
                    Div content = new Div();
                    
                    Icon infoIcon = new Icon(VaadinIcon.INFO_CIRCLE);
                    infoIcon.getStyle().set("margin-right", "var(--lumo-space-s)");
                    
                    Paragraph message = new Paragraph("⚠️ This project is deployed with Koyeb on a hobby plan, so it may work slow due to the low resources.");
                    message.getStyle().set("margin", "0");
                    
                    Button closeButton = new Button("Got it", new Icon(VaadinIcon.CHECK));
                    closeButton.addThemeVariants(ButtonVariant.LUMO_TERTIARY, ButtonVariant.LUMO_CONTRAST);
                    closeButton.getStyle().set("margin-left", "var(--lumo-space-m)");
                    
                    HorizontalLayout notificationLayout = new HorizontalLayout(infoIcon, message, closeButton);
                    notificationLayout.setAlignItems(Alignment.CENTER);
                    notificationLayout.setSpacing(true);
                    
                    content.add(notificationLayout);
                    
                    // Create the notification
                    Notification notification = new Notification(content);
                    notification.setDuration(0); // Make it persistent until closed
                    notification.setPosition(Notification.Position.TOP_CENTER);
                    notification.addThemeVariants(NotificationVariant.LUMO_PRIMARY);
                    
                    // Close button functionality
                    closeButton.addClickListener(event -> {
                        notification.close();
                        // Mark as shown in localStorage
                        ui.getPage().executeJs(
                            "localStorage.setItem('firstTimeNotificationShown', 'true');"
                        );
                    });
                    
                    notification.open();
                }
            });
        });
    }
}