# Spotify Playlist Generator

## Origin

This project started as a university assignment for the Web Technologies course at HTW Berlin (Summer Semester 2023). The original idea was a web app where you enter criteria — genre, artist, BPM, danceability — and receive a generated Spotify playlist of a chosen size. Generated playlists were saved to a database so users could retrieve them later. Optionally, a user could link their Spotify account and have the playlist created directly on it.

The backend was a Spring Boot REST API acting as a proxy to the Spotify Web API, backed by an H2 file database.

## Revival

Three years later the project has been rebuilt with a modern stack. The main driver was Spotify deprecating their `/v1/recommendations` endpoint in November 2024, which was the core of the original generation logic. Rather than find a workaround within the Spotify API, the approach is now:

1. **User describes** the playlist they want in plain language — any mood, theme, or context
2. **Claude (Anthropic's LLM)** generates a list of specific track and artist suggestions that fit the description
3. **Spotify Search** resolves each suggestion to a real Spotify track URI
4. **Spotify API** creates the playlist on the user's account and populates it

This gives far more expressive input than the old numeric seed parameters, and the quality of suggestions improves alongside the underlying model.

## Live

- **Frontend:** https://paulbailer.github.io/playlist-generator
- **Backend:** Render (auto-deployed on push to `main`)

## Stack

**Backend**
- Java 17 / Spring Boot 3.5
- PostgreSQL (production) / H2 in-memory (local dev)
- Anthropic Claude API — playlist and title generation
- Spotify Web API — track search, playlist creation
- Deployed on Render via Docker

**Frontend**
- React 19 / Vite 6
- Spotify OAuth 2.0 PKCE flow (no client secret in browser)
- Deployed on GitHub Pages via GitHub Actions

## Features

- Natural language prompt — describe any vibe, mood, or theme
- **Popularity** control — Underground → Deep cuts → Mixed → Popular → Mainstream
- **Energy** control — Very chill → Chill → Medium → Energetic → Intense
- **Era** filter — any combination of 60s through 2020s
- **Mood** tags — Happy, Melancholic, Angry, Romantic, Nostalgic, Focused
- Configurable track count (1–50)
- Max 2 songs per artist enforced server-side
- Playlist history scoped per Spotify user

## Project Structure

```
frontend/                        ← React app (GitHub Pages)
  src/
    auth/spotify.js              ← PKCE OAuth helpers
    services/api.js              ← backend API calls
    components/
      Login.jsx
      Generator.jsx
      PlaylistCard.jsx
      SegmentedSelector.jsx
      ChipSelector.jsx

src/main/java/app/               ← Spring Boot backend (Render)
  Application.java
  config/
    WebConfig.java               ← CORS
  playlist/
    Playlist.java                ← JPA entity
    PlaylistRepository.java
    PlaylistService.java
    PlaylistController.java      ← REST endpoints
    PlaylistGeneratorService.java
    ClaudeClient.java            ← Claude API
    SpotifyClient.java           ← Spotify API
    GeneratePlaylistRequest.java
    PlaylistSuggestion.java
    TrackSuggestion.java
```

## Running Locally

**Prerequisites:** Java 17, Node 18+, an Anthropic API key, a Spotify Developer App

**Backend**

```bash
export SPRING_PROFILES_ACTIVE=local
export ANTHROPIC_API_KEY=sk-ant-...
export ANTHROPIC_MODEL=claude-sonnet-4-6   # optional, this is the default

./gradlew bootRun
```

H2 console available at `http://localhost:8080/db-console`.

**Frontend**

Create `frontend/.env.local`:
```
VITE_SPOTIFY_CLIENT_ID=your_client_id
VITE_REDIRECT_URI=http://127.0.0.1:3000/callback
VITE_API_BASE_URL=http://localhost:8080
```

Add `http://127.0.0.1:3000/callback` as a redirect URI in your Spotify Developer App, then:

```bash
cd frontend && npm install && npm run dev
```

Open `http://127.0.0.1:3000` (not `localhost` — must match the redirect URI exactly).

## API

```
POST /generate-playlist
Authorization: Bearer <spotify_access_token>
Content-Type: application/json

{
  "prompt": "upbeat 90s rock for a road trip",
  "size": 20,
  "popularity": "deep_cuts",   // underground | deep_cuts | mixed | popular | mainstream
  "energy": "energetic",       // very_chill | chill | medium | energetic | very_energetic
  "era": ["90s", "2000s"],     // optional, any if omitted
  "mood": ["Happy"]            // optional, any if omitted
}
```

```
GET /playlists
Authorization: Bearer <spotify_access_token>
```

## Deployment

**Backend (Render)**

The repo includes a `Dockerfile`. On Render:

1. New → Web Service → connect this repo, runtime: Docker
2. New → PostgreSQL → use the internal connection details
3. Set environment variables:

| Variable | Value |
|---|---|
| `SPRING_DATASOURCE_URL` | `jdbc:postgresql://...` (internal URL from Render) |
| `SPRING_DATASOURCE_USERNAME` | from Render PostgreSQL |
| `SPRING_DATASOURCE_PASSWORD` | from Render PostgreSQL |
| `ANTHROPIC_API_KEY` | your Anthropic key |
| `ANTHROPIC_MODEL` | `claude-sonnet-4-6` |

**Frontend (GitHub Pages)**

Add these repository secrets (Settings → Secrets → Actions):

| Secret | Value |
|---|---|
| `VITE_SPOTIFY_CLIENT_ID` | your Spotify client ID |
| `VITE_API_BASE_URL` | your Render backend URL |

The workflow in `.github/workflows/deploy.yml` builds and deploys automatically on every push to `main` that touches `frontend/`. It can also be triggered manually from the Actions tab.

Add `https://paulbailer.github.io/playlist-generator` as a redirect URI in your Spotify Developer App.
