package org.kriba.news.service;

import org.kriba.analytics.repository.InteractionRepository;
import org.kriba.news.dto.NewsInfo;
import org.kriba.news.dto.NewsTotalArticles;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;

import java.util.*;

@Service
public class NewsInfoService {

    private final String apiKey;
    private final RestClient restClient;
    private final InteractionRepository interactionRepository;

    public NewsInfoService(@Value("${apiKey}") String apiKey, InteractionRepository interactionRepository) {
        this.apiKey = apiKey;
        this.interactionRepository = interactionRepository;
        this.restClient = RestClient.builder()
                .baseUrl("https://gnews.io/api/v4")
                .build();
    }

    public NewsTotalArticles getNewsByCategory(String category, int maxArticles) {
        NewsTotalArticles response = restClient.get()
                .uri("/top-headlines?lang=es&country=es&category=" + category + "&max=" + maxArticles + "&apikey=" + apiKey)
                .retrieve()
                .body(NewsTotalArticles.class);
        if (response != null && response.articles() != null) {
            response.articles().forEach(article -> article.setCategory(category));
        }
        return response != null ? response : new NewsTotalArticles(0L, Collections.emptyList());
    }


    public NewsTotalArticles getGeneralFeed(Long userId) {
        List<String> allCategories = List.of("general", "world", "nation", "business", "technology", "entertainment", "sports", "science", "health");
        int totalArticles = 45, minArticlesPerCategory = 2;
        int remainingArticlesToDistribute = totalArticles - (allCategories.size() * minArticlesPerCategory);

        List<NewsInfo> allArticles = new ArrayList<>();

        long totalPoints = 0;
        Map<String, Long> userPointsMap = new HashMap<>();

        if (userId != null) {
            List<Object[]> interactions = interactionRepository.countCategoriesByUserId(userId);
            for (Object[] row : interactions) {
                String cat = (String) row[0];
                long points = ((Number) row[1]).longValue();
                userPointsMap.put(cat, points);
                totalPoints += points;
            }
        }

        long granTotalGNews = 0;
        for (String cat : allCategories) {
            int amountToFetch = minArticlesPerCategory;
            if (totalPoints > 0) {
                long pointsInThisCategory = userPointsMap.getOrDefault(cat, 0L);
                double percentage = (double) pointsInThisCategory / totalPoints;
                amountToFetch += (int) Math.round(percentage * remainingArticlesToDistribute);
            } else {
                amountToFetch = totalArticles / allCategories.size();
            }

            try {
                NewsTotalArticles response = getNewsByCategory(cat, amountToFetch);
                if (response != null) {
                    if (response.totalArticles() != null) granTotalGNews += response.totalArticles();
                    if (response.articles() != null) allArticles.addAll(response.articles());
                }
                Thread.sleep(1000);
            } catch (Exception ex) {
                System.err.println("Error al cargar la categoría " + cat + ": " + ex.getMessage());
            }
        }

        Collections.shuffle(allArticles);
        return new NewsTotalArticles(granTotalGNews, allArticles);
    }
}
