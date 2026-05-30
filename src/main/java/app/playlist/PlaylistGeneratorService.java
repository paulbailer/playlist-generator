package app.playlist;

import org.springframework.stereotype.Service;

import java.util.*;
import java.util.stream.Collectors;

@Service
public class PlaylistGeneratorService {

    private final ClaudeClient claudeClient;
    private final SpotifyClient spotifyClient;
    private final PlaylistService playlistService;

    public PlaylistGeneratorService(ClaudeClient claudeClient, SpotifyClient spotifyClient, PlaylistService playlistService) {
        this.claudeClient = claudeClient;
        this.spotifyClient = spotifyClient;
        this.playlistService = playlistService;
    }

    public Playlist generate(GeneratePlaylistRequest request, String authHeader) {
        String criteria = buildCriteria(request);
        int fetchSize = (int) Math.ceil(request.size() * 1.5);
        PlaylistSuggestion suggestion = claudeClient.getSuggestions(request.prompt(), fetchSize, criteria);

        List<TrackSuggestion> filtered = capByArtist(suggestion.tracks(), 2);

        List<String> uris = filtered.stream()
            .map(s -> spotifyClient.searchTrackUri(s, authHeader))
            .filter(Optional::isPresent)
            .map(Optional::get)
            .limit(request.size())
            .collect(Collectors.toList());

        String title = safeTitle(suggestion.title(), request.prompt());
        String userId = spotifyClient.getUserId(authHeader);
        String playlistId = spotifyClient.createPlaylist(userId, title, authHeader);

        if (!uris.isEmpty()) {
            spotifyClient.addTracks(playlistId, uris, authHeader);
        }

        Playlist playlist = new Playlist();
        playlist.setName(title);
        playlist.setLink("https://open.spotify.com/playlist/" + playlistId);
        return playlistService.save(playlist);
    }

    private String buildCriteria(GeneratePlaylistRequest r) {
        List<String> parts = new ArrayList<>();

        if (r.popularity() != null) {
            parts.add(switch (r.popularity()) {
                case "underground" -> "Popularity: exclusively underground and obscure artists most people haven't heard of";
                case "deep_cuts"   -> "Popularity: deep cuts and B-sides, avoid mainstream hits";
                case "popular"     -> "Popularity: well-known popular songs";
                case "mainstream"  -> "Popularity: mainstream chart-topping hits";
                default            -> "Popularity: mix of well-known and lesser-known songs";
            });
        }

        if (r.energy() != null) {
            parts.add(switch (r.energy()) {
                case "very_chill"     -> "Energy: very relaxed, slow, and ambient";
                case "chill"          -> "Energy: laid-back and mellow";
                case "energetic"      -> "Energy: upbeat and energetic";
                case "very_energetic" -> "Energy: high-energy, intense, and driving";
                default               -> "Energy: moderate";
            });
        }

        if (r.era() != null && !r.era().isEmpty()) {
            parts.add("Era: primarily from the " + String.join(", ", r.era()));
        }

        if (r.mood() != null && !r.mood().isEmpty()) {
            parts.add("Mood: " + String.join(", ", r.mood()));
        }

        return String.join("\n", parts);
    }

    private String safeTitle(String title, String prompt) {
        if (title == null || title.isBlank() || title.equalsIgnoreCase("null")) {
            String[] words = prompt.trim().split("\\s+");
            String first = capitalize(words[0]);
            return words.length >= 2 ? first + " " + capitalize(words[1]) : first;
        }
        return title;
    }

    private String capitalize(String s) {
        return s.isEmpty() ? s : Character.toUpperCase(s.charAt(0)) + s.substring(1).toLowerCase();
    }

    private List<TrackSuggestion> capByArtist(List<TrackSuggestion> tracks, int max) {
        Map<String, Integer> counts = new HashMap<>();
        List<TrackSuggestion> result = new ArrayList<>();
        for (TrackSuggestion t : tracks) {
            String key = t.artist().toLowerCase();
            int count = counts.getOrDefault(key, 0);
            if (count < max) {
                result.add(t);
                counts.put(key, count + 1);
            }
        }
        return result;
    }
}
