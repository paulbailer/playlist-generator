package app.playlist;

import com.fasterxml.jackson.annotation.JsonProperty;

public record TrackResult(
    @JsonProperty("track") String track,
    @JsonProperty("artist") String artist,
    @JsonProperty("spotifyUrl") String spotifyUrl
) {}
