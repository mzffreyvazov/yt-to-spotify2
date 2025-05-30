package com.example.application.components;

import com.example.application.model.response.SpotifyResponse;
import com.vaadin.flow.component.ComponentEvent;
import com.vaadin.flow.component.ComponentEventListener;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.html.H4;
import com.vaadin.flow.component.html.Image;
import com.vaadin.flow.component.html.Paragraph;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.orderedlayout.FlexComponent.Alignment;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.shared.Registration;

public class SongCard extends Div {
    
    private final SpotifyResponse spotifyResponse;
    private Button addToPlaylistButton;
    private Button previewButton;
    private Button openSpotifyButton;
    
    public SongCard(SpotifyResponse spotifyResponse) {
        this.spotifyResponse = spotifyResponse;
        initializeComponent();
        setupStyling();
    }
    
    private void initializeComponent() {
        // Album image
        Image albumImage = new Image();
        albumImage.setSrc(spotifyResponse.getAlbumImageUrl() != null ? 
            spotifyResponse.getAlbumImageUrl() : "images/default-album.png");
        albumImage.setAlt("Album cover");
        albumImage.setWidth("80px");
        albumImage.setHeight("80px");
        
        // Song information
        H4 songTitle = new H4(spotifyResponse.getSongTitle());
        songTitle.getStyle().set("margin", "0").set("font-size", "1.1em");
        
        Paragraph artist = new Paragraph(spotifyResponse.getArtistName());
        artist.getStyle().set("margin", "0").set("color", "var(--lumo-secondary-text-color)");
        
        VerticalLayout songInfo = new VerticalLayout(songTitle, artist);
        songInfo.setSpacing(false);
        songInfo.setPadding(false);
        songInfo.setFlexGrow(1);
        
        // Action buttons
        createActionButtons();
        
        VerticalLayout buttonLayout = new VerticalLayout(previewButton, addToPlaylistButton, openSpotifyButton);
        buttonLayout.setSpacing(true);
        buttonLayout.setPadding(false);
        buttonLayout.setAlignItems(Alignment.END);
        
        // Main layout
        HorizontalLayout mainLayout = new HorizontalLayout(albumImage, songInfo, buttonLayout);
        mainLayout.setWidthFull();
        mainLayout.setAlignItems(Alignment.CENTER);
        mainLayout.setSpacing(true);
        
        add(mainLayout);
    }
    
    private void createActionButtons() {
        // Preview button
        previewButton = new Button(VaadinIcon.PLAY.create());
        previewButton.addThemeVariants(ButtonVariant.LUMO_SMALL, ButtonVariant.LUMO_TERTIARY);
        previewButton.setTooltipText("Preview");
        previewButton.setEnabled(spotifyResponse.getPreviewUrl() != null);
        previewButton.addClickListener(e -> handlePreview());
        
        // Add to playlist button
        addToPlaylistButton = new Button("Add to Playlist", VaadinIcon.PLUS.create());
        addToPlaylistButton.addThemeVariants(ButtonVariant.LUMO_SMALL, ButtonVariant.LUMO_PRIMARY);
        addToPlaylistButton.addClickListener(e -> handleAddToPlaylist());
        
        // Open in Spotify button
        openSpotifyButton = new Button(VaadinIcon.EXTERNAL_LINK.create());
        openSpotifyButton.addThemeVariants(ButtonVariant.LUMO_SMALL, ButtonVariant.LUMO_TERTIARY);
        openSpotifyButton.setTooltipText("Open in Spotify");
        openSpotifyButton.addClickListener(e -> handleOpenSpotify());
    }
    
    private void setupStyling() {
        addClassName("song-card");
        getStyle()
            .set("border", "1px solid var(--lumo-contrast-20pct)")
            .set("border-radius", "var(--lumo-border-radius-m)")
            .set("padding", "var(--lumo-space-m)")
            .set("margin-bottom", "var(--lumo-space-s)")
            .set("background", "var(--lumo-base-color)")
            .set("box-shadow", "0 1px 3px rgba(0, 0, 0, 0.1)")
            .set("transition", "box-shadow 0.2s ease-in-out");
        
        // Hover effect
        getElement().addEventListener("mouseenter", e -> 
            getStyle().set("box-shadow", "0 2px 8px rgba(0, 0, 0, 0.15)"));
        getElement().addEventListener("mouseleave", e -> 
            getStyle().set("box-shadow", "0 1px 3px rgba(0, 0, 0, 0.1)"));
    }
    
    private void handlePreview() {
        if (spotifyResponse.getPreviewUrl() != null) {
            getUI().ifPresent(ui -> 
                ui.getPage().executeJs("window.open($0, '_blank')", spotifyResponse.getPreviewUrl()));
        }
    }
    
    private void handleAddToPlaylist() {
        // Fire custom event that parent components can listen to
        fireEvent(new AddToPlaylistEvent(this, false, spotifyResponse));
    }
    
    private void handleOpenSpotify() {
        if (spotifyResponse.getSpotifyUrl() != null) {
            getUI().ifPresent(ui -> 
                ui.getPage().executeJs("window.open($0, '_blank')", spotifyResponse.getSpotifyUrl()));
        }
    }
    
    public SpotifyResponse getSpotifyResponse() {
        return spotifyResponse;
    }
    
    // Custom event for add to playlist action
    public static class AddToPlaylistEvent extends ComponentEvent<SongCard> {
        private final SpotifyResponse spotifyResponse;
        
        public AddToPlaylistEvent(SongCard source, boolean fromClient, SpotifyResponse spotifyResponse) {
            super(source, fromClient);
            this.spotifyResponse = spotifyResponse;
        }
        
        public SpotifyResponse getSpotifyResponse() {
            return spotifyResponse;
        }
    }
    
    // Method to register listener for add to playlist events
    public Registration addAddToPlaylistListener(ComponentEventListener<AddToPlaylistEvent> listener) {
        return addListener(AddToPlaylistEvent.class, listener);
    }
}
