package app.playlist;

import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface PlaylistRepository extends CrudRepository<Playlist, Long> {
    List<Playlist> findBySpotifyUserId(String spotifyUserId);
    void deleteBySpotifyUserId(String spotifyUserId);
}