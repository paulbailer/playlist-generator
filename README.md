# Spotify Playlist Generator [![Live](https://img.shields.io/badge/Live-brightgreen)](https://paulbailer.github.io/playlist-generator)

## Origin

This project started as a university assignment for the Web Technologies course at HTW Berlin (Summer Semester 2023). The original idea was a web app where you enter criteria — genre, artist, BPM, danceability — and receive a generated Spotify playlist of a chosen size. Generated playlists were saved to a database so users could retrieve them later. Optionally, a user could link their Spotify account and have the playlist created directly on it.

The backend was a Spring Boot REST API acting as a proxy to the Spotify Web API, backed by an H2 file database.

## Revival

Three years later the project has been rebuilt from the ground up. The main driver was Spotify deprecating their `/v1/recommendations` endpoint in November 2024, which was the core of the original generation logic.

Rather than find a workaround within the Spotify API, the replacement is an AI-powered approach using Anthropic's Claude model. The user describes what kind of playlist they want in plain language — a mood, a setting, a decade, an energy level — and Claude generates a curated list of specific tracks and artists that fit. Each suggestion is then verified against Spotify's catalog using the search API, so only songs that actually exist on the platform make it into the final playlist. The result is either a shareable track list with links to spotify, or — for users who connect their Spotify account — a playlist created directly on their profile.

This gives far more expressive input than the old numeric seed parameters (BPM floats, danceability scores), and the quality of suggestions naturally improves as the underlying model gets better.

## Live

- **Frontend:** https://paulbailer.github.io/playlist-generator
- **Backend:** Render (auto-deployed on push to `main`)

## Stack

**Backend**
- Java 17 / Spring Boot 3.5
- PostgreSQL (production) / H2 in-memory (local dev)
- Anthropic Claude API — track selection and playlist title generation
- Spotify Web API — catalog search and playlist creation
- Deployed on Render via Docker

**Frontend**
- React 19 / Vite 6
- Spotify OAuth 2.0 PKCE flow (no client secret in browser)
- Deployed on GitHub Pages via GitHub Actions

## Features

- **No login required** — anyone can generate a track list with direct Spotify links
- **Connect Spotify** to upgrade: playlists are created directly on your Spotify account
- Natural language prompt — describe any vibe, mood, or theme
- **Popularity** control — Underground → Deep cuts → Mixed → Popular → Mainstream
- **Energy** control — Very chill → Chill → Medium → Energetic → Intense
- **Era** filter — any combination of 60s through 2020s
- **Mood** tags — Happy, Melancholic, Angry, Romantic, Nostalgic, Focused
- Configurable track count (1–50)
- Max 2 songs per artist enforced server-side
- Playlist history scoped per Spotify user
- All user data deleted from the database on logout
- Friendly error shown if a Spotify account isn't authorised for the app

## Project Structure

```
frontend/                        ← React app (GitHub Pages)
  src/
    auth/spotify.js              ← PKCE OAuth helpers
    services/api.js              ← backend API calls
    components/
      Generator.jsx              ← main UI, handles guest + authenticated modes
      TrackList.jsx              ← track list with multi-platform links
      PlaylistCard.jsx
      SegmentedSelector.jsx
      ChipSelector.jsx
      Privacy.jsx

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
    SpotifyClient.java           ← Spotify API (user + Client Credentials)
    GeneratePlaylistRequest.java
    PlaylistSuggestion.java
    SuggestionResponse.java
    TrackSuggestion.java
    TrackResult.java
```

## Running Locally

**Prerequisites:** Java 17, Node 18+, an Anthropic API key, a Spotify Developer App

**Backend**

```bash
export SPRING_PROFILES_ACTIVE=local
export ANTHROPIC_API_KEY=sk-ant-...
export ANTHROPIC_MODEL=claude-sonnet-4-6   # optional, this is the default
export SPOTIFY_CLIENT_ID=...               # needed for the guest /suggest endpoint
export SPOTIFY_CLIENT_SECRET=...

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

**Guest — no authentication required**
```
POST /suggest
Content-Type: application/json

{
  "prompt": "upbeat 90s rock for a road trip",
  "size": 20,
  "popularity": "deep_cuts",
  "energy": "energetic",
  "era": ["90s"],
  "mood": ["Happy"]
}
```
Returns `{ "title": "...", "tracks": [{ "track": "...", "artist": "...", "spotifyUrl": "..." }] }`

**Authenticated — creates playlist on Spotify**
```
POST /generate-playlist
Authorization: Bearer <spotify_access_token>
Content-Type: application/json

{ "prompt": "...", "size": 20, "popularity": "mixed", "energy": "chill", "era": [], "mood": [] }
```

```
GET /playlists
Authorization: Bearer <spotify_access_token>
```

```
DELETE /user-data
Authorization: Bearer <spotify_access_token>
```

Criteria values:
- `popularity`: `underground` | `deep_cuts` | `mixed` | `popular` | `mainstream`
- `energy`: `very_chill` | `chill` | `medium` | `energetic` | `very_energetic`
- `era` / `mood`: optional arrays, omit or leave empty for no filter

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
| `SPOTIFY_CLIENT_ID` | your Spotify app client ID |
| `SPOTIFY_CLIENT_SECRET` | your Spotify app client secret |

**Frontend (GitHub Pages)**

Add these repository secrets (Settings → Secrets → Actions):

| Secret | Value |
|---|---|
| `VITE_SPOTIFY_CLIENT_ID` | your Spotify client ID |
| `VITE_API_BASE_URL` | your Render backend URL |

The workflow in `.github/workflows/deploy.yml` builds and deploys automatically on every push to `main` that touches `frontend/`. It can also be triggered manually from the Actions tab.

Add `https://paulbailer.github.io/playlist-generator` as a redirect URI in your Spotify Developer App.

## Note on Spotify Access

Due to Spotify's development mode restrictions (as of May 2025, extended quota is only available to companies), full playlist creation is limited to accounts manually added in the Spotify Developer Dashboard. The app handles this gracefully — anyone can use the guest mode to generate track lists with multi-platform links, and unauthorized users who attempt to connect their Spotify account see a clear message explaining how to request access.
