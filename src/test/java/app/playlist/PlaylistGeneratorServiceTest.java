package app.playlist;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class PlaylistGeneratorServiceTest {

    @Mock ClaudeClient claudeClient;
    @Mock SpotifyClient spotifyClient;
    @Mock PlaylistService playlistService;

    @InjectMocks
    PlaylistGeneratorService service;

    @Test
    void suggest_returnsTrackListWithTitle() {
        var suggestions = List.of(new TrackSuggestion("Everlong", "Foo Fighters"));
        when(claudeClient.getSuggestions(any(), anyInt(), any()))
            .thenReturn(new PlaylistSuggestion("Rainy Drive", suggestions));
        when(spotifyClient.searchTrack(suggestions.get(0)))
            .thenReturn(Optional.of(new TrackResult("Everlong", "Foo Fighters", "https://open.spotify.com/track/1")));

        SuggestionResponse result = service.suggest(request("rock", 1));

        assertThat(result.title()).isEqualTo("Rainy Drive");
        assertThat(result.tracks()).hasSize(1);
        assertThat(result.tracks().get(0).track()).isEqualTo("Everlong");
    }

    @Test
    void generate_savesPlaylistWithTitleFromClaude() {
        var tracks = List.of(new TrackSuggestion("Everlong", "Foo Fighters"));
        when(claudeClient.getSuggestions(eq("rock"), anyInt(), any())).thenReturn(new PlaylistSuggestion("Rainy Day Drive", tracks));
        when(spotifyClient.searchTrackUri(tracks.get(0), "Bearer tok")).thenReturn(Optional.of("spotify:track:abc123"));
        when(spotifyClient.getUserId("Bearer tok")).thenReturn("user1");
        when(spotifyClient.createPlaylist("user1", "Rainy Day Drive", "Bearer tok")).thenReturn("pl1");
        when(playlistService.save(any())).thenAnswer(inv -> inv.getArgument(0));

        Playlist result = service.generate(request("rock", 1), "Bearer tok");

        assertThat(result.getName()).isEqualTo("Rainy Day Drive");
        assertThat(result.getLink()).isEqualTo("https://open.spotify.com/playlist/pl1");
    }

    @Test
    void generate_addsOnlyTracksFoundOnSpotify() {
        var tracks = List.of(
            new TrackSuggestion("Real Song", "Real Artist"),
            new TrackSuggestion("Fake Song", "Fake Artist")
        );
        when(claudeClient.getSuggestions(any(), anyInt(), any())).thenReturn(new PlaylistSuggestion("Mixed Bag", tracks));
        when(spotifyClient.searchTrackUri(tracks.get(0), "Bearer tok")).thenReturn(Optional.of("spotify:track:real"));
        when(spotifyClient.searchTrackUri(tracks.get(1), "Bearer tok")).thenReturn(Optional.empty());
        when(spotifyClient.getUserId(any())).thenReturn("u");
        when(spotifyClient.createPlaylist(any(), any(), any())).thenReturn("pl");
        when(playlistService.save(any())).thenAnswer(inv -> inv.getArgument(0));

        service.generate(request("mix", 2), "Bearer tok");

        verify(spotifyClient).addTracks("pl", List.of("spotify:track:real"), "Bearer tok");
    }

    @Test
    void generate_capsTracksPerArtistAtTwo() {
        var tracks = List.of(
            new TrackSuggestion("Song A", "Same Artist"),
            new TrackSuggestion("Song B", "Same Artist"),
            new TrackSuggestion("Song C", "Same Artist")
        );
        when(claudeClient.getSuggestions(any(), anyInt(), any())).thenReturn(new PlaylistSuggestion("Artist Cap Test", tracks));
        when(spotifyClient.searchTrackUri(any(), any())).thenReturn(Optional.of("spotify:track:x"));
        when(spotifyClient.getUserId(any())).thenReturn("u");
        when(spotifyClient.createPlaylist(any(), any(), any())).thenReturn("pl");
        when(playlistService.save(any())).thenAnswer(inv -> inv.getArgument(0));

        service.generate(request("test", 3), "Bearer tok");

        verify(spotifyClient).addTracks(eq("pl"), argThat(list -> list.size() == 2), eq("Bearer tok"));
    }

    @Test
    void generate_skipsAddTracks_whenNoUrisResolved() {
        var tracks = List.of(new TrackSuggestion("Unknown", "Nobody"));
        when(claudeClient.getSuggestions(any(), anyInt(), any())).thenReturn(new PlaylistSuggestion("Hidden Gems", tracks));
        when(spotifyClient.searchTrackUri(any(), any())).thenReturn(Optional.empty());
        when(spotifyClient.getUserId(any())).thenReturn("u");
        when(spotifyClient.createPlaylist(any(), any(), any())).thenReturn("pl");
        when(playlistService.save(any())).thenAnswer(inv -> inv.getArgument(0));

        service.generate(request("obscure", 1), "Bearer tok");

        verify(spotifyClient, never()).addTracks(any(), any(), any());
    }

    @Test
    void generate_persistsPlaylistToDatabase() {
        when(claudeClient.getSuggestions(any(), anyInt(), any())).thenReturn(new PlaylistSuggestion("Chill Vibes", List.of()));
        when(spotifyClient.getUserId(any())).thenReturn("u");
        when(spotifyClient.createPlaylist(any(), any(), any())).thenReturn("pl");
        when(playlistService.save(any())).thenAnswer(inv -> inv.getArgument(0));

        service.generate(request("chill", 10), "Bearer tok");

        verify(playlistService).save(any(Playlist.class));
    }

    @Test
    void generate_includesCriteriaInClaudeCall() {
        when(claudeClient.getSuggestions(any(), anyInt(), any())).thenReturn(new PlaylistSuggestion("Title", List.of()));
        when(spotifyClient.getUserId(any())).thenReturn("u");
        when(spotifyClient.createPlaylist(any(), any(), any())).thenReturn("pl");
        when(playlistService.save(any())).thenAnswer(inv -> inv.getArgument(0));

        var req = new GeneratePlaylistRequest("test", 5, "underground", "very_energetic", List.of("90s"), List.of("Angry"));
        service.generate(req, "Bearer tok");

        verify(claudeClient).getSuggestions(eq("test"), anyInt(), argThat(criteria ->
            criteria.contains("underground") && criteria.contains("90s") && criteria.contains("Angry")
        ));
    }

    private GeneratePlaylistRequest request(String prompt, int size) {
        return new GeneratePlaylistRequest(prompt, size, null, null, null, null);
    }
}
