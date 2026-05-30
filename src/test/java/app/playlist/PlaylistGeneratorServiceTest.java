package app.playlist;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class PlaylistGeneratorServiceTest {

    @Mock ClaudeClient claudeClient;
    @Mock SpotifyClient spotifyClient;
    @Mock PlaylistService playlistService;

    @InjectMocks
    PlaylistGeneratorService service;

    @Test
    void generate_savesPlaylistWithCorrectNameAndLink() {
        var suggestions = List.of(new TrackSuggestion("Everlong", "Foo Fighters"));
        when(claudeClient.getSuggestions("rock", 1)).thenReturn(suggestions);
        when(spotifyClient.searchTrackUri(suggestions.get(0), "Bearer tok"))
            .thenReturn(Optional.of("spotify:track:abc123"));
        when(spotifyClient.getUserId("Bearer tok")).thenReturn("user1");
        when(spotifyClient.createPlaylist("user1", "rock", "Bearer tok")).thenReturn("pl1");
        when(playlistService.save(any())).thenAnswer(inv -> inv.getArgument(0));

        Playlist result = service.generate("rock", 1, "Bearer tok");

        assertThat(result.getName()).isEqualTo("rock");
        assertThat(result.getLink()).isEqualTo("https://open.spotify.com/playlist/pl1");
    }

    @Test
    void generate_addsOnlyTracksFoundOnSpotify() {
        var suggestions = List.of(
            new TrackSuggestion("Real Song", "Real Artist"),
            new TrackSuggestion("Fake Song", "Fake Artist")
        );
        when(claudeClient.getSuggestions("mix", 2)).thenReturn(suggestions);
        when(spotifyClient.searchTrackUri(suggestions.get(0), "Bearer tok"))
            .thenReturn(Optional.of("spotify:track:real"));
        when(spotifyClient.searchTrackUri(suggestions.get(1), "Bearer tok"))
            .thenReturn(Optional.empty());
        when(spotifyClient.getUserId("Bearer tok")).thenReturn("u");
        when(spotifyClient.createPlaylist("u", "mix", "Bearer tok")).thenReturn("pl");
        when(playlistService.save(any())).thenAnswer(inv -> inv.getArgument(0));

        service.generate("mix", 2, "Bearer tok");

        verify(spotifyClient).addTracks("pl", List.of("spotify:track:real"), "Bearer tok");
    }

    @Test
    void generate_skipsAddTracks_whenNoUrisResolved() {
        var suggestions = List.of(new TrackSuggestion("Unknown", "Nobody"));
        when(claudeClient.getSuggestions("obscure", 1)).thenReturn(suggestions);
        when(spotifyClient.searchTrackUri(any(), any())).thenReturn(Optional.empty());
        when(spotifyClient.getUserId(any())).thenReturn("u");
        when(spotifyClient.createPlaylist(any(), any(), any())).thenReturn("pl");
        when(playlistService.save(any())).thenAnswer(inv -> inv.getArgument(0));

        service.generate("obscure", 1, "Bearer tok");

        verify(spotifyClient, never()).addTracks(any(), any(), any());
    }

    @Test
    void generate_persistsPlaylistToDatabase() {
        when(claudeClient.getSuggestions(any(), anyInt())).thenReturn(List.of());
        when(spotifyClient.getUserId(any())).thenReturn("u");
        when(spotifyClient.createPlaylist(any(), any(), any())).thenReturn("pl");
        when(playlistService.save(any())).thenAnswer(inv -> inv.getArgument(0));

        service.generate("chill", 10, "Bearer tok");

        verify(playlistService).save(any(Playlist.class));
    }
}