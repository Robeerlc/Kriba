package org.kriba.summarize.service;

import org.kriba.users.model.User;
import org.kriba.users.repository.UserRepository;
import org.springframework.beans.factory.annotation.Value;
import org.kriba.summarize.dto.SummarizeOutDTO;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.util.List;
import java.util.Map;

@Service
public class SummarizeService {

    private final RestClient restClient;
    private final String model;
    private final UserRepository userRepository;
    private final String apiKeySummarize;
    private final ObjectMapper objectMapper;

    public SummarizeService(
            @Value("${apiKeySummarize}") String apiKeySummarize,
            @Value("${geminiModel}") String model,
            UserRepository userRepository) {
        this.restClient = RestClient.builder()
                .baseUrl("https://generativelanguage.googleapis.com/v1beta")
                .defaultHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
                .defaultHeader(HttpHeaders.ACCEPT, MediaType.APPLICATION_JSON_VALUE)
                .build();
        this.model = model;
        this.userRepository = userRepository;
        this.apiKeySummarize = apiKeySummarize;
        this.objectMapper = new ObjectMapper();
    }

    public SummarizeOutDTO summarize(Long currentUserId, String textContent, String articleUrl) {
        User user = userRepository.findById(currentUserId)
                .orElseThrow(() -> new IllegalArgumentException("No existe usuario con ese ID"));

        if (user.getDailyAiLimit() <= 0) {
            throw new IllegalArgumentException("¡No te quedan mas intentos! Espera a mañana");
        }


        String rawResponse = restClient.post()
                .uri(uriBuilder -> uriBuilder
                        .path("/models/{geminiModel}:generateContent")
                        .queryParam("key", apiKeySummarize)
                        .build(model))
                .body(initializePrompt(textContent, articleUrl))
                .retrieve()
                .body(String.class);


        String cleanSummary = extractTextFromGeminiResponse(rawResponse);

        user.setDailyAiLimit(user.getDailyAiLimit() - 1);
        userRepository.save(user);

        return SummarizeOutDTO.builder()
                .summary(cleanSummary)
                .remainingDailyUses(user.getDailyAiLimit())
                .build();
    }

    private String extractTextFromGeminiResponse(String rawResponse) {
        try {
            JsonNode root = objectMapper.readTree(rawResponse);
            // La estructura de Google es: candidates[0].content.parts[0].text
            return root.path("candidates")
                    .get(0)
                    .path("content")
                    .path("parts")
                    .get(0)
                    .path("text")
                    .asText();
        } catch (Exception e) {
            throw new RuntimeException("Error al parsear la respuesta de Gemini. ¿El JSON cambió?", e);
        }
    }

    private Map<String, Object> initializePrompt(String text, String url) {
        String prompt = """
                You are an assistant specialized in summarizing and simplifying texts.

                Your task is to transform long texts into shorter, clearer, and easier-to-understand versions without losing the important information.

                Rules:
                Reduce the length of the text while keeping the main ideas but not much.
                Do not remove important data, dates, names, numbers, or key concepts.
                Do not invent new information.
                Do not change the original meaning.
                Use clear and direct sentences.
                Avoid repetition and unnecessary words.
                Keep a neutral, informative, and professional tone.
                If the original text is confusing, summarize the main idea as clearly as possible.
                Return only the summarized text, without extra explanations.

                Text:
                %s

                Article URL:
                %s
                """.formatted(text, url);

        return Map.of(
                "contents", List.of(
                        Map.of(
                                "parts", List.of(
                                        Map.of("text", prompt)
                                )
                        )
                )
        );
    }
}