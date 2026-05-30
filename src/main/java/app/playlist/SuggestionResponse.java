package app.playlist;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.List;

public record SuggestionResponse(
    @JsonProperty("title") String title,
    @JsonProperty("tracks") List<TrackResult> tracks
) {}
