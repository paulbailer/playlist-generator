package app.playlist;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

import java.util.List;
import java.util.Map;

@Component
public class ClaudeClient {

    private final String apiKey;
    private final String model;
    private final RestClient restClient;
    private final ObjectMapper objectMapper = new ObjectMapper();

    public ClaudeClient(
            @Value("${anthropic.api.key}") String apiKey,
            @Value("${anthropic.model}") String model,
            RestClient.Builder builder) {
        this.apiKey = apiKey;
        this.model = model;
        this.restClient = builder.build();
    }

    public List<TrackSuggestion> getSuggestions(String prompt, int size) {
        String userMessage = String.format(
            "Generate a playlist of exactly %d songs matching this description: \"%s\". " +
            "Return ONLY a JSON array, no other text. " +
            "Each element must have 'track' and 'artist' string fields. " +
            "Choose well-known songs that are very likely to be on Spotify. " +
            "Example format: [{\"track\": \"Bohemian Rhapsody\", \"artist\": \"Queen\"}]",
            size, prompt
        );

        Map<String, Object> body = Map.of(
            "model", model,
            "max_tokens", 1024,
            "messages", List.of(Map.of("role", "user", "content", userMessage))
        );

        String response = restClient.post()
            .uri("https://api.anthropic.com/v1/messages")
            .header("x-api-key", apiKey)
            .header("anthropic-version", "2023-06-01")
            .contentType(MediaType.APPLICATION_JSON)
            .body(body)
            .retrieve()
            .body(String.class);

        try {
            JsonNode root = objectMapper.readTree(response);
            String text = root.path("content").get(0).path("text").asText().strip();
            if (text.startsWith("```")) {
                text = text.replaceAll("(?s)^```[a-zA-Z]*\\n?", "").replaceAll("```\\s*$", "").strip();
            }
            return objectMapper.readValue(text,
                objectMapper.getTypeFactory().constructCollectionType(List.class, TrackSuggestion.class));
        } catch (Exception e) {
            throw new RuntimeException("Failed to parse Claude response: " + e.getMessage(), e);
        }
    }
}