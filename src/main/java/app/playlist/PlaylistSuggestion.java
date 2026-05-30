package app.playlist;

import java.util.List;

public record PlaylistSuggestion(String title, List<TrackSuggestion> tracks) {}
