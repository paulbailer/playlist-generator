package app.playlist;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.List;

public record PlaylistSuggestion(
    @JsonProperty("title") String title,
    @JsonProperty("tracks") List<TrackSuggestion> tracks
) {}
