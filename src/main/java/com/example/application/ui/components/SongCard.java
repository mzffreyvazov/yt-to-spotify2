package com.example.application.ui.components;

import com.example.application.model.response.SpotifyResponse;
import com.example.application.model.response.YoutubeResponse;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.html.H4;
import com.vaadin.flow.component.html.Image;
import com.vaadin.flow.component.html.Paragraph;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.notification.NotificationVariant;
import com.vaadin.flow.component.orderedlayout.FlexComponent.Alignment;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;

public class SongCard extends Div {

    private final SpotifyResponse spotifyResponse;
    private final YoutubeResponse youtubeResponse; // Placeholder for future use
    private Button copyLinkButton;
    private Button openSpotifyButton;   
    
    public SongCard(SpotifyResponse spotifyResponse) {
        this.spotifyResponse = spotifyResponse;
        this.youtubeResponse = null;
        initializeComponent();
        setupStyling();
    }

    public SongCard(YoutubeResponse video) {
        this.spotifyResponse = null;
        this.youtubeResponse = video;
        initializeComponentYT();
        setupStyling();
    }

    private void initializeComponent() {
        
        Image albumImage = new Image();
        albumImage.setSrc(spotifyResponse.getAlbumImageUrl() != null ?
                spotifyResponse.getAlbumImageUrl() : "images/default-album.png");
        albumImage.setAlt("Album cover");
        albumImage.setWidth("80px"); 
        albumImage.setHeight("80px"); 
        albumImage.getStyle().set("border-radius", "var(--lumo-border-radius-s)"); 
        albumImage.getStyle().set("object-fit", "cover"); 

        
        H4 songTitle = new H4(spotifyResponse.getSongTitle());
        songTitle.getStyle()
                .set("margin-top", "0")
                .set("margin-bottom", "var(--lumo-space-xs)") 
                .set("font-size", "1.1em")
                .set("font-weight", "600"); 

        Paragraph artist = new Paragraph(spotifyResponse.getArtistName());
        artist.getStyle()
                .set("margin", "0")
                .set("font-size", "0.9em")
                .set("color", "var(--lumo-secondary-text-color)");

        VerticalLayout songInfoLayout = new VerticalLayout(songTitle, artist);
        songInfoLayout.setSpacing(false);
        songInfoLayout.setPadding(false);

        
        createActionButtons();

        HorizontalLayout buttonsLayout = new HorizontalLayout(copyLinkButton, openSpotifyButton);
        buttonsLayout.setSpacing(true);
        buttonsLayout.setPadding(false);
        buttonsLayout.getStyle().set("margin-top", "var(--lumo-space-s)"); 

        
        VerticalLayout rightPaneLayout = new VerticalLayout(songInfoLayout, buttonsLayout);
        rightPaneLayout.setSpacing(false); 
        rightPaneLayout.setPadding(false);
        rightPaneLayout.setFlexGrow(1, songInfoLayout); 
        rightPaneLayout.setWidthFull();


        
        HorizontalLayout mainLayout = new HorizontalLayout(albumImage, rightPaneLayout);
        mainLayout.setWidthFull();
        mainLayout.setAlignItems(Alignment.CENTER); 
        mainLayout.setSpacing(true);
        mainLayout.getStyle().set("padding", "var(--lumo-space-s)"); 

        add(mainLayout);
    }

    private void createActionButtons() {
        
        copyLinkButton = new Button("Copy Link", VaadinIcon.COPY_O.create());
        copyLinkButton.addThemeVariants(ButtonVariant.LUMO_SMALL, ButtonVariant.LUMO_TERTIARY_INLINE); 
        copyLinkButton.setTooltipText("Copy Spotify link");
        copyLinkButton.setEnabled(spotifyResponse.getSpotifyUrl() != null && !spotifyResponse.getSpotifyUrl().isEmpty());
        copyLinkButton.addClickListener(e -> handleCopyLink());

        
        openSpotifyButton = new Button("Open", VaadinIcon.EXTERNAL_LINK.create()); 
        openSpotifyButton.addThemeVariants(ButtonVariant.LUMO_SMALL, ButtonVariant.LUMO_PRIMARY); 
        openSpotifyButton.setTooltipText("Open in Spotify");
        openSpotifyButton.setEnabled(spotifyResponse.getSpotifyUrl() != null && !spotifyResponse.getSpotifyUrl().isEmpty());
        openSpotifyButton.addClickListener(e -> handleOpenSpotify());
    }

    private void setupStyling() {
        addClassName("song-card");
        getStyle()
                .set("border", "1px solid var(--lumo-contrast-10pct)") 
                .set("border-radius", "var(--lumo-border-radius-m)")
                .set("padding", "var(--lumo-space-s)") 
                .set("margin-bottom", "var(--lumo-space-m)") 
                .set("background", "var(--lumo-base-color)")
                .set("box-shadow", "var(--lumo-box-shadow-xs)") 
                .set("transition", "box-shadow 0.2s ease-in-out");

        
        getElement().addEventListener("mouseenter", e ->
                getStyle().set("box-shadow", "var(--lumo-box-shadow-s)"));
        getElement().addEventListener("mouseleave", e ->
                getStyle().set("box-shadow", "var(--lumo-box-shadow-xs)"));
    }

    private void handleCopyLink() {
        if (spotifyResponse.getSpotifyUrl() != null && !spotifyResponse.getSpotifyUrl().isEmpty()) {
            String script = String.format(
                "navigator.clipboard.writeText('%s').then(() => {" +
                "  console.log('Spotify link copied to clipboard');" +
                "}).catch(err => {" +
                "  console.error('Failed to copy Spotify link: ', err);" +
                "});",
                spotifyResponse.getSpotifyUrl()
            );
            getUI().ifPresent(ui -> ui.getPage().executeJs(script));
            Notification.show("Spotify link copied!", 2000, Notification.Position.BOTTOM_STRETCH)
                .addThemeVariants(NotificationVariant.LUMO_SUCCESS);
        } else {
             Notification.show("No Spotify link available to copy.", 3000, Notification.Position.BOTTOM_STRETCH)
                .addThemeVariants(NotificationVariant.LUMO_WARNING);
        }
    }

    private void handleOpenSpotify() {
        if (spotifyResponse.getSpotifyUrl() != null && !spotifyResponse.getSpotifyUrl().isEmpty()) {
            getUI().ifPresent(ui ->
                    ui.getPage().executeJs("window.open($0, '_blank')", spotifyResponse.getSpotifyUrl()));
        }
    }

    private void initializeComponentYT() {
        Image thumbnailImage = new Image();
        thumbnailImage.setSrc(youtubeResponse.getThumbnailUrl() != null ?
                youtubeResponse.getThumbnailUrl() : "images/default-thumbnail.png");
        thumbnailImage.setAlt("Video thumbnail");
        thumbnailImage.setWidth("80px"); 
        thumbnailImage.setHeight("80px"); 
        thumbnailImage.getStyle().set("border-radius", "var(--lumo-border-radius-s)"); 
        thumbnailImage.getStyle().set("object-fit", "cover"); 

        H4 songTitle = new H4(youtubeResponse.getSongTitle());
        songTitle.getStyle()
                .set("margin-top", "0")
                .set("margin-bottom", "var(--lumo-space-xs)") 
                .set("font-size", "1.1em")
                .set("font-weight", "600"); 

        Paragraph artist = new Paragraph(youtubeResponse.getArtistName());
        artist.getStyle()
                .set("margin", "0")
                .set("font-size", "0.9em")
                .set("color", "var(--lumo-secondary-text-color)");

        VerticalLayout songInfoLayout = new VerticalLayout(songTitle, artist);
        songInfoLayout.setSpacing(false);
        songInfoLayout.setPadding(false);

        createYouTubeActionButtons();

        HorizontalLayout buttonsLayout = new HorizontalLayout(copyLinkButton, openSpotifyButton);
        buttonsLayout.setSpacing(true);
        buttonsLayout.setPadding(false);
        buttonsLayout.getStyle().set("margin-top", "var(--lumo-space-s)"); 

        VerticalLayout rightPaneLayout = new VerticalLayout(songInfoLayout, buttonsLayout);
        rightPaneLayout.setSpacing(false); 
        rightPaneLayout.setPadding(false);
        rightPaneLayout.setFlexGrow(1, songInfoLayout); 
        rightPaneLayout.setWidthFull();

        HorizontalLayout mainLayout = new HorizontalLayout(thumbnailImage, rightPaneLayout);
        mainLayout.setWidthFull();
        mainLayout.setAlignItems(Alignment.CENTER); 
        mainLayout.setSpacing(true);
        mainLayout.getStyle().set("padding", "var(--lumo-space-s)"); 

        add(mainLayout);
    }

    private void createYouTubeActionButtons() {
        copyLinkButton = new Button("Copy Link", VaadinIcon.COPY_O.create());
        copyLinkButton.addThemeVariants(ButtonVariant.LUMO_SMALL, ButtonVariant.LUMO_TERTIARY_INLINE); 
        copyLinkButton.setTooltipText("Copy YouTube link");
        copyLinkButton.setEnabled(youtubeResponse.getVideoId() != null);
        copyLinkButton.addClickListener(e -> handleCopyYouTubeLink());

        openSpotifyButton = new Button("Open", VaadinIcon.EXTERNAL_LINK.create()); 
        openSpotifyButton.addThemeVariants(ButtonVariant.LUMO_SMALL, ButtonVariant.LUMO_PRIMARY); 
        openSpotifyButton.setTooltipText("Open in YouTube");
        openSpotifyButton.setEnabled(youtubeResponse.getVideoId() != null);
        openSpotifyButton.addClickListener(e -> handleOpenYouTube());
    }

    private void handleCopyYouTubeLink() {
        if (youtubeResponse.getVideoId() != null) {
            String youtubeUrl = "https://www.youtube.com/watch?v=" + youtubeResponse.getVideoId();
            String script = String.format(
                "navigator.clipboard.writeText('%s').then(() => {" +
                "  console.log('YouTube link copied to clipboard');" +
                "}).catch(err => {" +
                "  console.error('Failed to copy YouTube link: ', err);" +
                "});",
                youtubeUrl
            );
            getUI().ifPresent(ui -> ui.getPage().executeJs(script));
            Notification.show("YouTube link copied!", 2000, Notification.Position.BOTTOM_STRETCH)
                .addThemeVariants(NotificationVariant.LUMO_SUCCESS);
        } else {
             Notification.show("No YouTube link available to copy.", 3000, Notification.Position.BOTTOM_STRETCH)
                .addThemeVariants(NotificationVariant.LUMO_WARNING);
        }
    }

    private void handleOpenYouTube() {
        if (youtubeResponse.getVideoId() != null) {
            String youtubeUrl = "https://www.youtube.com/watch?v=" + youtubeResponse.getVideoId();
            getUI().ifPresent(ui ->
                    ui.getPage().executeJs("window.open($0, '_blank')", youtubeUrl));
        }
    }

    public SpotifyResponse getSpotifyResponse() {
        return spotifyResponse;
    }

    public YoutubeResponse getYoutubeResponse() {
        return youtubeResponse;
    }
    
}