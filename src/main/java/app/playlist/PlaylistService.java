package app.playlist;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
public class PlaylistService {

    @Autowired
    PlaylistRepository repo;

    public Playlist save(Playlist playlist) {
        return repo.save(playlist);
    }

    public Playlist get(Long id) {
        return repo.findById(id).orElseThrow(RuntimeException::new);
    }

    public List<Playlist> getAll() {
        List<Playlist> playlists = new ArrayList<>();
        repo.findAll().forEach(playlists::add);
        return playlists;
    }

    public List<Playlist> getByUser(String spotifyUserId) {
        return repo.findBySpotifyUserId(spotifyUserId);
    }
}