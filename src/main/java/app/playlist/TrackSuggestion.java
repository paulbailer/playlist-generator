package app.playlist;

import com.fasterxml.jackson.annotation.JsonProperty;

public record TrackSuggestion(
    @JsonProperty("track") String track,
    @JsonProperty("artist") String artist
) {}
