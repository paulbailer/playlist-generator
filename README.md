# Spotify Playlist Generator

## Origin

This project started as a university assignment for the Web Technologies course at HTW Berlin (Summer Semester 2023). The original idea was a web app where you enter criteria — genre, artist, BPM, danceability — and receive a generated Spotify playlist of a chosen size. Generated playlists were saved to a database so users could retrieve them later. Optionally, a user could link their Spotify account and have the playlist created directly on it.

The backend was a Spring Boot REST API acting as a proxy to the Spotify Web API, backed by an H2 file database.

## Revival

Three years later the project is being rebuilt from the ground up with a modern stack. The main driver was Spotify deprecating their `/v1/recommendations` endpoint in November 2024, which was the core of the original generation logic. Rather than find a workaround within the Spotify API, the approach is now:

1. **User describes** the playlist they want in plain language — any mood, theme, or context
2. **Claude (Anthropic's LLM)** generates a list of specific track and artist suggestions that fit the description
3. **Spotify Search** resolves each suggestion to a real Spotify track URI
4. **Spotify API** creates the playlist on the user's account and populates it

This gives far more expressive input than the old numeric seed parameters, and the quality of suggestions improves alongside the underlying model.

## Stack

**Backend**
- Java 17 / Spring Boot 3.5
- PostgreSQL (production) / H2 (local dev)
- Anthropic Claude API for playlist generation
- Spotify Web API for search and playlist creation
- Deployed on Render

**Frontend** *(in progress)*
- React
- Deployed on GitHub Pages

## Project Structure

```
src/
  main/java/app/
    Application.java          ← entry point
    config/
      WebConfig.java          ← CORS configuration
    playlist/
      Playlist.java           ← JPA entity
      PlaylistRepository.java
      PlaylistService.java    ← database operations
      PlaylistController.java ← REST endpoints
      PlaylistGeneratorService.java ← orchestrates generation flow
      ClaudeClient.java       ← Claude API integration
      SpotifyClient.java      ← Spotify API integration
      GeneratePlaylistRequest.java
      TrackSuggestion.java
  test/java/app/
    playlist/                 ← unit + slice tests
```

## Running Locally

**Prerequisites:** Java 17, an Anthropic API key, a Spotify Developer App

Set environment variables:
```bash
export SPRING_PROFILES_ACTIVE=local   # uses H2, no Postgres needed
export ANTHROPIC_API_KEY=sk-ant-...
export ANTHROPIC_MODEL=claude-sonnet-4-6   # optional, this is the default
export SPOTIFY_CLIENT_ID=...
export SPOTIFY_CLIENT_SECRET=...
```

```bash
./gradlew bootRun
```

The H2 console is available at `http://localhost:8080/db-console` when running with the local profile.

## Key Endpoint

```
POST /generate-playlist
Authorization: Bearer <spotify_access_token>
Content-Type: application/json

{ "prompt": "upbeat 90s rock for a road trip", "size": 20 }
```

Returns the saved playlist with a direct Spotify link.

## Deployment (Render)

The repo includes a `Dockerfile`. On Render:

1. Create a **Web Service** pointing to this repo — Render detects the Dockerfile automatically
2. Create a **PostgreSQL** instance and link it to the service
3. Set environment variables:
   - `SPRING_DATASOURCE_URL` — Render's internal JDBC URL (`jdbc:postgresql://...`)
   - `SPRING_DATASOURCE_USERNAME`
   - `SPRING_DATASOURCE_PASSWORD`
   - `ANTHROPIC_API_KEY`
   - `SPOTIFY_CLIENT_ID`
   - `SPOTIFY_CLIENT_SECRET`