package app.playlist;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(PlaylistController.class)
class PlaylistControllerTest {

    @Autowired MockMvc mockMvc;
    @Autowired ObjectMapper objectMapper;

    @MockitoBean PlaylistService service;
    @MockitoBean PlaylistGeneratorService generatorService;

    @Test
    void getAllPlaylists_returns200WithList() throws Exception {
        when(service.getAll()).thenReturn(List.of(
            playlistWith("Rock Mix", "https://open.spotify.com/playlist/1"),
            playlistWith("Chill Vibes", "https://open.spotify.com/playlist/2")
        ));

        mockMvc.perform(get("/playlists"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.length()").value(2))
            .andExpect(jsonPath("$[0].name").value("Rock Mix"))
            .andExpect(jsonPath("$[1].name").value("Chill Vibes"));
    }

    @Test
    void getPlaylist_returns200WithPlaylist() throws Exception {
        when(service.get(1L)).thenReturn(playlistWith("Jazz Night", "https://open.spotify.com/playlist/3"));

        mockMvc.perform(get("/playlists/1"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.name").value("Jazz Night"))
            .andExpect(jsonPath("$.link").value("https://open.spotify.com/playlist/3"));
    }

    @Test
    void createPlaylist_returns200WithSavedPlaylist() throws Exception {
        Playlist input = playlistWith("My Playlist", "https://open.spotify.com/playlist/4");
        when(service.save(any())).thenReturn(input);

        mockMvc.perform(post("/playlists")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(input)))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.name").value("My Playlist"));
    }

    @Test
    void generatePlaylist_returns200WithGeneratedPlaylist() throws Exception {
        Playlist generated = playlistWith("upbeat rock", "https://open.spotify.com/playlist/abc");
        when(generatorService.generate(anyString(), anyInt(), anyString())).thenReturn(generated);

        mockMvc.perform(post("/generate-playlist")
                .header("Authorization", "Bearer test-token")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(new GeneratePlaylistRequest("upbeat rock", 10))))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.name").value("upbeat rock"))
            .andExpect(jsonPath("$.link").value("https://open.spotify.com/playlist/abc"));
    }

    @Test
    void generatePlaylist_passesAuthHeaderToService() throws Exception {
        when(generatorService.generate(eq("lo-fi"), eq(5), eq("Bearer my-token")))
            .thenReturn(playlistWith("lo-fi", "https://open.spotify.com/playlist/xyz"));

        mockMvc.perform(post("/generate-playlist")
                .header("Authorization", "Bearer my-token")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(new GeneratePlaylistRequest("lo-fi", 5))))
            .andExpect(status().isOk());
    }

    private Playlist playlistWith(String name, String link) {
        Playlist p = new Playlist();
        p.setName(name);
        p.setLink(link);
        return p;
    }
}