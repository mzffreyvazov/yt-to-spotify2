# GitHub Copilot Instructions for yt-to-spotify2

## Project Overview

A Spring Boot 4.0 + Vaadin 24 application that converts music links between YouTube and Spotify. Users paste a YouTube link to find matching Spotify tracks (or vice versa).

## NEVER EVER DO

These rules are ABSOLUTE:

### NEVER Publish Sensitive Data
- NEVER publish passwords, API keys, tokens to public repos
- Before ANY commit: verify no secrets included

### NEVER Commit .env Files
- NEVER commit `.env` or `env.properties` to git
- ALWAYS verify `.env` and `env.properties` are in `.gitignore`

### NEVER Hardcode Credentials
- ALWAYS use environment variables

## Architecture

### Data Flow
```
MainView (Vaadin UI) → REST API (/api/links/*) → LinkProcessorService → LinkConvertorService
                                                        ↓
                                              SpotifyService / YoutubeService
                                                        ↓
                                              External APIs (Spotify/YouTube)
```

### Key Layers
- **UI** ([src/main/java/com/example/application/ui](src/main/java/com/example/application/ui)): Vaadin components using server-side Java
- **Controllers** ([controller/](src/main/java/com/example/application/controller)): REST endpoints at `/api/links/*`
- **Services** ([service/](src/main/java/com/example/application/service)): Business logic with reactive `Mono<>` return types
- **Models** ([model/](src/main/java/com/example/application/model)): DTOs split into `queries/`, `response/`, `spotify_dto/`, `youtube_dto/`

## Critical Patterns

### Reactive WebClient Usage
All external API calls use Spring WebFlux `WebClient` with `Mono<>`. Services are injected with qualified beans:
```java
@Qualifier("searchWebClientSpotify") WebClient searchWebClient
@Qualifier("trackWebClientYoutube") WebClient trackWebClient
```
WebClient beans are configured in [ApiWebClientsConfig.java](src/main/java/com/example/application/config/ApiWebClientsConfig.java).

### Configuration Properties Pattern
API credentials use `@ConfigurationProperties` classes (e.g., `SpotifyProperties`, `YoutubeProperties`). Properties are bound from `application.properties` with prefixes like `spotify.*` and `youtube.*`.

### Fallback Search Strategy
`LinkProcessorService` implements progressive fallback searches—if specific queries fail, it falls back to general queries, then title-only queries. See `searchSpotifyWithFallbacks()` and `searchYoutubeWithFallbacks()`.

### Vaadin UI Components
- UI is server-side Java (no separate frontend framework)
- Custom components extend `Div` (e.g., `SongCard`)
- Use `getUI().ifPresent(ui -> ui.access(() -> {...}))` for async UI updates

## Developer Workflows

### Running Locally
```bash
# Development mode (hot reload enabled)
./mvnw spring-boot:run

# Production build
./mvnw -Pproduction package
java -jar target/yt-to-spotify-1.0-SNAPSHOT.jar
```

### API Keys Setup
Create `src/main/resources/env.properties` (gitignored):
```properties
SPOTIFY_CLIENT_ID=your_id
SPOTIFY_CLIENT_SECRET=your_secret
YOUTUBE_API_KEY=your_key
```

### Code Formatting
Project uses Spotless with Eclipse formatter. Run `./mvnw spotless:apply` before committing.

## Project Conventions

- **Lombok**: Use `@Data`, `@AllArgsConstructor`, `@NoArgsConstructor` for DTOs
- **Response DTOs**: Simplified DTOs in `model/response/` (e.g., `SpotifyResponse`) vs raw API DTOs in `model/spotify_dto/`
- **Logging**: Use `System.out.println` for debug logging (visible in console during development)
- **URL Parsing**: Regex patterns in `LinkConvertorService` for extracting video/track IDs

## Key Files Reference

| Purpose | File |
|---------|------|
| Main entry point | [Application.java](src/main/java/com/example/application/Application.java) |
| Main UI view | [MainView.java](src/main/java/com/example/application/ui/views/MainView.java) |
| Link conversion logic | [LinkConvertorService.java](src/main/java/com/example/application/service/LinkConvertorService.java) |
| REST endpoints | [LinkProcessingController.java](src/main/java/com/example/application/controller/LinkProcessingController.java) |
| WebClient config | [ApiWebClientsConfig.java](src/main/java/com/example/application/config/ApiWebClientsConfig.java) |
| App config | [application.properties](src/main/resources/application.properties) |

## Deployment

Deployed on Koyeb. Docker builds use [Dockerfile](Dockerfile) with `eclipse-temurin:21-jre`. Production builds require the `-Pproduction` Maven profile to bundle Vaadin frontend assets.

