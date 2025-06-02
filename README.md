# YouTube to Spotify Music Converter

This application allows users to convert music links between YouTube and Spotify. Paste a YouTube link to find matching tracks on Spotify, or (in a future version) paste a Spotify link to find the corresponding YouTube video.

## Demo
https://github.com/user-attachments/assets/73b30c5d-d574-47a2-8cae-de261c4d813f

## Features

*   Convert YouTube music video links to Spotify track suggestions.
*   User-friendly interface built with Vaadin.
*   (Planned) Convert Spotify tracks to YouTube video links.

## Prerequisites

*   Java 17 or higher
*   Maven
*   Spotify API Credentials (Client ID and Client Secret)
*   YouTube Data API v3 Key

## Setup

1.  **Clone the repository:**
    ```bash
    git clone <repository-url>
    cd yt-to-spotify2
    ```

2.  **Configure API Keys:**
    *   Create a file named `env.properties` in the `src/main/resources` directory.
    *   Add your API keys to this file in the following format:
        ```properties
        SPOTIFY_CLIENT_ID=your_spotify_client_id
        SPOTIFY_CLIENT_SECRET=your_spotify_client_secret
        YOUTUBE_API_KEY=your_youtube_api_key
        ```
    *   **Important:** Ensure `env.properties` is added to your `.gitignore` file to prevent committing your API keys.

## Running the Application

### Development Mode

1.  **From your IDE:**
    *   Import the project as a Maven project.
    *   Locate and run the `com.example.application.Application` class.
2.  **From the command line:**
    ```bash
    ./mvnw spring-boot:run
    ```
The application will be accessible at `http://localhost:8080`.

### Production Mode

1.  **Build the application:**
    ```bash
    ./mvnw -Pproduction package
    ```
2.  **Run the packaged application:**
    ```bash
    java -jar target/yt-to-spotify2.jar
    ```

## Project Structure

*   `src/main/java`: Contains the Java source code.
    *   `com.example.application.Application.java`: Main Spring Boot application class.
    *   `com.example.application.views`: Vaadin UI views.
    *   `com.example.application.components`: Custom UI components.
    *   `com.example.application.service`: Business logic and API integrations.
    *   `com.example.application.model`: Data models (request/response objects).
    *   `com.example.application.config`: Application configuration (e.g., WebClient, CORS).
*   `src/main/resources`: Contains static resources and configuration files.
    *   `env.properties` (you need to create this): For API keys.
*   `pom.xml`: Maven project configuration.

## Current Status

*   YouTube to Spotify conversion is functional.
*   Spotify to YouTube conversion is **not yet implemented**.
