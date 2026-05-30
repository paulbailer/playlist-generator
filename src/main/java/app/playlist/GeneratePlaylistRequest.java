package app.playlist;

import java.util.List;

public record GeneratePlaylistRequest(
    String prompt,
    int size,
    String popularity,
    String energy,
    List<String> era,
    List<String> mood
) {}
