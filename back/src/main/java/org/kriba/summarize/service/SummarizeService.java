package org.kriba.summarize.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.transaction.Transactional;
import org.kriba.analytics.model.Interaction;
import org.kriba.analytics.repository.InteractionRepository;
import org.kriba.summarize.dto.SummarizeResponse;
import org.kriba.users.model.User;
import org.kriba.users.repository.UserRepository;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;

import java.util.List;
import java.util.Map;

@Service
public class SummarizeService {

    private final RestClient restClient;
    private final String model;
    private final UserRepository userRepository;
    private final String apiKeySummarize;
    private final ObjectMapper objectMapper;
    private final InteractionRepository interactionRepository;

    public SummarizeService(
            @Value("${apiKeySummarize}") String apiKeySummarize,
            @Value("${geminiModel}") String model,
            UserRepository userRepository, InteractionRepository interactionRepository) {
        this.interactionRepository = interactionRepository;
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

    @Transactional
    @Cacheable(value = "summarizeCache", key = "#textContent + '-' + #articleUrl")
    public SummarizeResponse summarize(Long currentUserId, String textContent, String articleUrl, String category, String externalArticleId) {
        User user = userRepository.findById(currentUserId)
                .orElseThrow(() -> new IllegalArgumentException("Usuario no encontrado"));

        if (user.getDailyAiLimit() <= 0)
            throw new IllegalArgumentException("No te quedan intentos restantes");

        String rawResponse = restClient.post()
                .uri(uriBuilder -> uriBuilder
                        .path("/models/{geminiModel}:generateContent")
                        .queryParam("key", apiKeySummarize)
                        .build(model))
                .body(initializePrompt(textContent, articleUrl))
                .retrieve()
                .body(String.class);


        String cleanSummary = extractTextFromGeminiResponse(rawResponse);
        Interaction interaction = Interaction.builder()
                .userId(currentUserId)
                .articleCategory(category)
                .externalArticleId(externalArticleId)
                .interactionType("SUMMARIZE")
                .build();
        interactionRepository.save(interaction);
        user.setDailyAiLimit(user.getDailyAiLimit() - 1);
        userRepository.save(user);

        return SummarizeResponse.builder()
                .summary(cleanSummary)
                .remainingDailyUses(user.getDailyAiLimit())
                .build();
    }

    private String extractTextFromGeminiResponse(String rawResponse) {
        try {
            JsonNode root = objectMapper.readTree(rawResponse);
            JsonNode candidates = root.path("candidates");
            if (!candidates.isArray() || candidates.isEmpty())
                throw new IllegalArgumentException("Respuesta inesperada de la IA de Gemini: Faltan los candidatos");

            JsonNode firstCandidate = candidates.get(0);
            JsonNode parts = firstCandidate
                    .path("content")
                    .path("parts");

            if (!parts.isArray() || parts.isEmpty())
                throw new IllegalArgumentException("Respuesta inesperada de la IA de Gemini: Falta el contenido");

            String summary = parts.get(0)
                    .path("text")
                    .asText();

            if (summary == null || summary.isBlank())
                throw new IllegalArgumentException("Respuesta inesperada de la IA de Gemini: Texto vacío");
            return summary.trim();
        } catch (JsonProcessingException e) {
            throw new IllegalArgumentException("Error al procesar el JSON de la IA de Gemini", e);
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