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

    public PlaylistSuggestion getSuggestions(String prompt, int size, String criteria) {
        String criteriaSection = criteria.isBlank() ? "" : "Additional criteria:\n" + criteria + "\n\n";
        String userMessage = String.format(
            "Generate a playlist of %d songs matching this description: \"%s\".\n\n" +
            "%s" +
            "Return ONLY a raw JSON object — no markdown, no code fences, no explanation:\n" +
            "{\"title\":\"<2-word playlist title that reflects the prompt>\",\"tracks\":[{\"track\":\"<song>\",\"artist\":\"<artist>\"}]}\n\n" +
            "Guidelines:\n" +
            "- Only suggest songs you are confident exist on Spotify. Do not invent titles.\n" +
            "- If you are unsure whether a specific song is on Spotify, choose a different track by the same artist instead.\n" +
            "- No more than 2 songs per artist\n" +
            "- Pick tracks that genuinely fit the mood and description",
            size, prompt, criteriaSection
        );

        Map<String, Object> body = Map.of(
            "model", model,
            "max_tokens", 2048,
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
            return objectMapper.readValue(text, PlaylistSuggestion.class);
        } catch (Exception e) {
            throw new RuntimeException("Failed to parse Claude response: " + e.getMessage(), e);
        }
    }
}