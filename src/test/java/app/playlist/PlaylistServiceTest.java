package app.playlist;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class PlaylistServiceTest {

    @Mock
    PlaylistRepository repo;

    @InjectMocks
    PlaylistService service;

    @Test
    void save_delegatesToRepository() {
        Playlist playlist = playlistWith("Rock Mix", "https://open.spotify.com/playlist/1");
        when(repo.save(playlist)).thenReturn(playlist);

        Playlist result = service.save(playlist);

        assertThat(result).isSameAs(playlist);
        verify(repo).save(playlist);
    }

    @Test
    void get_returnsPlaylist_whenFound() {
        Playlist playlist = playlistWith("Jazz Night", "https://open.spotify.com/playlist/2");
        when(repo.findById(1L)).thenReturn(Optional.of(playlist));

        Playlist result = service.get(1L);

        assertThat(result.getName()).isEqualTo("Jazz Night");
    }

    @Test
    void get_throwsRuntimeException_whenNotFound() {
        when(repo.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.get(99L))
            .isInstanceOf(RuntimeException.class);
    }

    @Test
    void getAll_returnsAllPlaylists() {
        Playlist a = playlistWith("A", "link-a");
        Playlist b = playlistWith("B", "link-b");
        when(repo.findAll()).thenReturn(List.of(a, b));

        List<Playlist> result = service.getAll();

        assertThat(result).hasSize(2);
        assertThat(result).extracting(Playlist::getName).containsExactly("A", "B");
    }

    @Test
    void getByUser_returnsOnlyThatUsersPlaylists() {
        Playlist p = playlistWith("Rock Mix", "link");
        when(repo.findBySpotifyUserId("user123")).thenReturn(List.of(p));

        List<Playlist> result = service.getByUser("user123");

        assertThat(result).hasSize(1);
        assertThat(result.get(0).getName()).isEqualTo("Rock Mix");
    }

    @Test
    void deleteByUser_delegatesToRepository() {
        service.deleteByUser("user123");
        verify(repo).deleteBySpotifyUserId("user123");
    }

    @Test
    void getAll_returnsEmptyList_whenRepositoryIsEmpty() {
        when(repo.findAll()).thenReturn(List.of());

        assertThat(service.getAll()).isEmpty();
    }

    private Playlist playlistWith(String name, String link) {
        Playlist p = new Playlist();
        p.setName(name);
        p.setLink(link);
        return p;
    }
}