package app.playlist;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.*;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.client.RestTemplate;

import java.util.Collections;
import java.util.List;
import java.util.Map;

@RestController
public class PlaylistController {

    @Autowired
    PlaylistService service;

    @Autowired
    PlaylistGeneratorService generatorService;

    @Autowired
    SpotifyClient spotifyClient;

    Logger logger = LoggerFactory.getLogger(PlaylistController.class);

    @PostMapping("/suggest")
    public ResponseEntity<SuggestionResponse> suggest(@RequestBody GeneratePlaylistRequest request) {
        return ResponseEntity.ok(generatorService.suggest(request));
    }

    @PostMapping("/generate-playlist")
    public ResponseEntity<Playlist> generatePlaylist(
            @RequestHeader("Authorization") String authHeader,
            @RequestBody GeneratePlaylistRequest request) {
        return ResponseEntity.ok(generatorService.generate(request, authHeader));
    }

    @PostMapping("/playlists")
    public Playlist createPlaylist(@RequestBody Playlist playlist) {
        return service.save(playlist);
    }

    @GetMapping("/playlists/{id}")
    public Playlist getPlaylist(@PathVariable String id) {
        logger.info("GET /playlists/{}", id);
        return service.get(Long.parseLong(id));
    }

    @DeleteMapping("/user-data")
    public ResponseEntity<Void> deleteUserData(@RequestHeader("Authorization") String authHeader) {
        String userId = spotifyClient.getUserId(authHeader);
        service.deleteByUser(userId);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/playlists")
    public List<Playlist> getAllPlaylists(@RequestHeader("Authorization") String authHeader) {
        String userId = spotifyClient.getUserId(authHeader);
        return service.getByUser(userId);
    }

    @GetMapping("/user")
    public ResponseEntity<String> getAPIUser(@RequestHeader("Authorization") String authorizationHeader) {
        String spotifyUrl = "https://api.spotify.com/v1/me";
        try {
            HttpHeaders headers = new HttpHeaders();
            headers.set("Authorization", authorizationHeader);
            RestTemplate restTemplate = new RestTemplate();
            ResponseEntity<String> response = restTemplate.exchange(spotifyUrl, HttpMethod.GET, new HttpEntity<>(headers), String.class);
            return ResponseEntity.ok(response.getBody());
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Error forwarding request to Spotify");
        }
    }

    @PostMapping("/create/playlist")
    public ResponseEntity<String> createSpotifyPlaylist(
            @RequestHeader("Authorization") String authorizationHeader,
            @RequestHeader("Content-Type") String contentType,
            @RequestParam("name") String name,
            @RequestParam("user") String user) {
        String spotifyUrl = "https://api.spotify.com/v1/users/" + user + "/playlists";
        try {
            HttpHeaders headers = new HttpHeaders();
            headers.set("Authorization", authorizationHeader);
            headers.set("Content-Type", contentType);
            RestTemplate restTemplate = new RestTemplate();
            ResponseEntity<String> response = restTemplate.postForEntity(
                spotifyUrl, new HttpEntity<>(Collections.singletonMap("name", name), headers), String.class);
            return ResponseEntity.ok(response.getBody());
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Error forwarding request to Spotify");
        }
    }

    @PostMapping("/add")
    public ResponseEntity<String> addTracks(
            @RequestHeader("Authorization") String authorizationHeader,
            @RequestHeader("Content-Type") String contentType,
            @RequestParam("id") String id,
            @RequestBody String requestBody) {
        String spotifyUrl = "https://api.spotify.com/v1/playlists/" + id + "/tracks";
        try {
            HttpHeaders headers = new HttpHeaders();
            headers.set("Authorization", authorizationHeader);
            headers.set("Content-Type", contentType);
            RestTemplate restTemplate = new RestTemplate();
            ResponseEntity<String> response = restTemplate.exchange(
                spotifyUrl, HttpMethod.POST, new HttpEntity<>(requestBody, headers), String.class);
            return ResponseEntity.ok(response.getBody());
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Error forwarding request to Spotify");
        }
    }
}
