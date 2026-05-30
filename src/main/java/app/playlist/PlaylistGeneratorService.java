package app.playlist;

import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;
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

    public Playlist generate(String prompt, int size, String authHeader) {
        List<TrackSuggestion> suggestions = claudeClient.getSuggestions(prompt, size);

        List<String> uris = suggestions.stream()
            .map(s -> spotifyClient.searchTrackUri(s, authHeader))
            .filter(Optional::isPresent)
            .map(Optional::get)
            .collect(Collectors.toList());

        String userId = spotifyClient.getUserId(authHeader);
        String playlistId = spotifyClient.createPlaylist(userId, prompt, authHeader);

        if (!uris.isEmpty()) {
            spotifyClient.addTracks(playlistId, uris, authHeader);
        }

        Playlist playlist = new Playlist();
        playlist.setName(prompt);
        playlist.setLink("https://open.spotify.com/playlist/" + playlistId);
        return playlistService.save(playlist);
    }
}