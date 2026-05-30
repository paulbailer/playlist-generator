package app.playlist;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.List;

public record GeneratePlaylistRequest(
    @JsonProperty("prompt")     String prompt,
    @JsonProperty("size")       int size,
    @JsonProperty("popularity") String popularity,
    @JsonProperty("energy")     String energy,
    @JsonProperty("era")        List<String> era,
    @JsonProperty("mood")       List<String> mood
) {}
