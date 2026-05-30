package app.playlist;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;
import org.springframework.web.util.UriComponentsBuilder;

import java.net.URI;
import java.util.Base64;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@Component
public class SpotifyClient {

    private static final Logger logger = LoggerFactory.getLogger(SpotifyClient.class);

    @Value("${spotify.client.id:}")
    private String clientId;

    @Value("${spotify.client.secret:}")
    private String clientSecret;

    private final RestClient restClient;
    private final ObjectMapper objectMapper = new ObjectMapper();

    private volatile String cachedAppToken = null;
    private volatile long tokenExpiresAt = 0;

    public SpotifyClient(RestClient.Builder builder) {
        this.restClient = builder.build();
    }

    // ── Unauthenticated (Client Credentials) ───────────────────────────────

    private synchronized String getAppToken() {
        if (cachedAppToken != null && System.currentTimeMillis() < tokenExpiresAt) {
            return cachedAppToken;
        }
        if (clientId.isEmpty() || clientSecret.isEmpty()) {
            throw new RuntimeException("Spotify client credentials not configured");
        }
        String credentials = Base64.getEncoder()
            .encodeToString((clientId + ":" + clientSecret).getBytes());

        String response = restClient.post()
            .uri("https://accounts.spotify.com/api/token")
            .header("Authorization", "Basic " + credentials)
            .contentType(MediaType.APPLICATION_FORM_URLENCODED)
            .body("grant_type=client_credentials")
            .retrieve()
            .body(String.class);

        try {
            JsonNode root = objectMapper.readTree(response);
            cachedAppToken = root.path("access_token").asText();
            long expiresIn = root.path("expires_in").asLong(3600);
            tokenExpiresAt = System.currentTimeMillis() + (expiresIn - 60) * 1000L;
            return cachedAppToken;
        } catch (Exception e) {
            throw new RuntimeException("Failed to get Spotify app token", e);
        }
    }

    public Optional<TrackResult> searchTrack(TrackSuggestion suggestion) {
        try {
            String appToken = "Bearer " + getAppToken();
            return searchWithToken(suggestion, appToken);
        } catch (Exception e) {
            logger.warn("App-token search failed for: {} by {} — {}", suggestion.track(), suggestion.artist(), e.getMessage());
            return Optional.empty();
        }
    }

    // ── Authenticated (user token) ──────────────────────────────────────────

    public Optional<String> searchTrackUri(TrackSuggestion suggestion, String authHeader) {
        return searchWithToken(suggestion, authHeader)
            .map(r -> "spotify:track:" + extractTrackId(r.spotifyUrl()));
    }

    // ── Shared search logic ─────────────────────────────────────────────────

    private Optional<TrackResult> searchWithToken(TrackSuggestion suggestion, String authHeader) {
        try {
            URI uri = UriComponentsBuilder.fromUriString("https://api.spotify.com/v1/search")
                .queryParam("q", "track:" + suggestion.track() + " artist:" + suggestion.artist())
                .queryParam("type", "track")
                .queryParam("limit", "1")
                .build()
                .encode()
                .toUri();

            String response = restClient.get()
                .uri(uri)
                .header("Authorization", authHeader)
                .retrieve()
                .body(String.class);

            JsonNode items = objectMapper.readTree(response).path("tracks").path("items");
            if (items.isArray() && !items.isEmpty()) {
                JsonNode item = items.get(0);
                String track = item.path("name").asText();
                String artist = item.path("artists").get(0).path("name").asText();
                String spotifyUrl = item.path("external_urls").path("spotify").asText();
                return Optional.of(new TrackResult(track, artist, spotifyUrl));
            }
            logger.warn("No Spotify result for: {} by {}", suggestion.track(), suggestion.artist());
            return Optional.empty();
        } catch (Exception e) {
            logger.warn("Spotify search failed for: {} by {} — {}", suggestion.track(), suggestion.artist(), e.getMessage());
            return Optional.empty();
        }
    }

    private String extractTrackId(String spotifyUrl) {
        // https://open.spotify.com/track/<id> → <id>
        return spotifyUrl.substring(spotifyUrl.lastIndexOf('/') + 1);
    }

    // ── User-authenticated endpoints ────────────────────────────────────────

    public String getUserId(String authHeader) {
        String response = restClient.get()
            .uri("https://api.spotify.com/v1/me")
            .header("Authorization", authHeader)
            .retrieve()
            .body(String.class);

        try {
            return objectMapper.readTree(response).path("id").asText();
        } catch (Exception e) {
            throw new RuntimeException("Failed to get Spotify user ID", e);
        }
    }

    public String createPlaylist(String userId, String title, String authHeader) {
        Map<String, String> body = Map.of("name", title, "description", "");

        String response = restClient.post()
            .uri("https://api.spotify.com/v1/users/" + userId + "/playlists")
            .header("Authorization", authHeader)
            .contentType(MediaType.APPLICATION_JSON)
            .body(body)
            .retrieve()
            .body(String.class);

        try {
            return objectMapper.readTree(response).path("id").asText();
        } catch (Exception e) {
            throw new RuntimeException("Failed to create Spotify playlist", e);
        }
    }

    public void addTracks(String playlistId, List<String> uris, String authHeader) {
        for (int i = 0; i < uris.size(); i += 100) {
            List<String> batch = uris.subList(i, Math.min(i + 100, uris.size()));
            restClient.post()
                .uri("https://api.spotify.com/v1/playlists/" + playlistId + "/tracks")
                .header("Authorization", authHeader)
                .contentType(MediaType.APPLICATION_JSON)
                .body(Map.of("uris", batch))
                .retrieve()
                .toBodilessEntity();
        }
    }
}
