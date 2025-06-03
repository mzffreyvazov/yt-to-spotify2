# YouTube to Spotify Music Converter

This application allows users to convert music links between YouTube and Spotify. Paste a YouTube link to find matching tracks on Spotify, or (in a future version) paste a Spotify link to find the corresponding YouTube video.

## 🚀 Live Demo

**Try it now:** [https://afraid-mariejeanne-mzffreyvazov-db9cf636.koyeb.app](https://afraid-mariejeanne-mzffreyvazov-db9cf636.koyeb.app)

## Video Demo
https://github.com/user-attachments/assets/73b30c5d-d574-47a2-8cae-de261c4d813f

## Features

*   Convert YouTube music video links to Spotify track suggestions.
*   User-friendly interface built with Vaadin.
*   (Planned) Convert Spotify tracks to YouTube video links.
*   **Deployed on Koyeb** - Access from anywhere!

## Prerequisites

*   Java 17 or higher
*   Maven
*   Spotify API Credentials (Client ID and Client Secret)
*   YouTube Data API v3 Key

## Setup

### For Local Development

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

### For Production Deployment (Koyeb)

The application is already deployed at: **[https://afraid-mariejeanne-mzffreyvazov-db9cf636.koyeb.app](https://afraid-mariejeanne-mzffreyvazov-db9cf636.koyeb.app)**

To deploy your own instance:
1. Fork this repository
2. Connect your GitHub repository to [Koyeb](https://www.koyeb.com/)
3. Set the following environment variables in Koyeb:
   - `SPOTIFY_CLIENT_ID`
   - `SPOTIFY_CLIENT_SECRET`
   - `YOUTUBE_API_KEY`
4. Deploy using these settings:
   - **Build Command**: `./mvnw clean package -Pproduction -DskipTests`
   - **Run Command**: `java -jar target/yt-to-spotify-1.0-SNAPSHOT.jar`
   - **Port**: `8080`

## Running the Application Locally

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
    *   `com.example.application.ui.views`: Vaadin UI views.
    *   `com.example.application.ui.components`: Custom UI components.
    *   `com.example.application.service`: Business logic and API integrations.
    *   `com.example.application.model`: Data models (request/response objects).
    *   `com.example.application.config`: Application configuration (e.g., WebClient, CORS).
*   `src/main/resources`: Contains static resources and configuration files.
    *   `env.properties` (you need to create this): For API keys.
*   `pom.xml`: Maven project configuration.

## Technologies Used

- **Backend**: Spring Boot, Java 21
- **Frontend**: Vaadin Framework
- **APIs**: Spotify Web API, YouTube Data API v3
- **Deployment**: Koyeb (Cloud Platform)
- **Build Tool**: Maven
