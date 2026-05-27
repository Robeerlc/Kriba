package org.kriba.news.service;

import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.kriba.analytics.repository.InteractionRepository;
import org.kriba.news.dto.NewsInfo;
import org.kriba.news.dto.NewsTotalArticles;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;

import java.util.*;
import java.util.stream.Collectors;

@Service
public class NewsInfoService {
    private final String apiKey;
    private final RestClient restClient;
    private final InteractionRepository interactionRepository;

    @Autowired
    @Lazy
    private NewsInfoService selfProxy;

    public NewsInfoService(@Value("${apiKey}") String apiKey, InteractionRepository interactionRepository) {
        this.apiKey = apiKey;
        this.interactionRepository = interactionRepository;
        this.restClient = RestClient.builder().baseUrl("https://gnews.io/api/v4").build();
    }

    @Cacheable(value = "newsByCategory", key = "{#category, #maxArticles}")
    @CircuitBreaker(name = "gnewsApi", fallbackMethod = "fallbackGetNewsByCategory")
    public NewsTotalArticles getNewsByCategory(String category, int maxArticles) {
        NewsTotalArticles response = restClient.get()
                .uri("/top-headlines?lang=es&country=es&category=" + category + "&max=" + maxArticles + "&apikey=" + apiKey)
                .retrieve()
                .body(NewsTotalArticles.class);
        try {
            Thread.sleep(1000);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }

        if (response != null && response.articles() != null) {
            response.articles().forEach(article -> article.setCategory(category));
            response.articles().parallelStream().forEach(this::scrapFullText);
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
            List<InteractionRepository.CategoryStats> interactions = interactionRepository.getCategoryStatsByUserId(userId);
            for (InteractionRepository.CategoryStats stat : interactions) {
                String cat = stat.getCategory();
                long points = stat.getPoints();
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

            NewsTotalArticles response = selfProxy.getNewsByCategory(cat, amountToFetch);
            if (response != null) {
                if (response.totalArticles() != null) granTotalGNews += response.totalArticles();
                if (response.articles() != null) allArticles.addAll(response.articles());
            }
        }
        Collections.shuffle(allArticles);
        return new NewsTotalArticles(granTotalGNews, allArticles);
    }

    public void scrapFullText(NewsInfo article) {
        try {
            Document doc = Jsoup.connect(article.getUrl())
                    .userAgent("Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/114.0.0.0 Safari/537.36")
                    .timeout(5000)
                    .get();

            String text = doc.select("article p, main p, [class*=article] p, [class*=content] p, [class*=body] p, p")
                    .stream()
                    .map(Element::text)
                    .filter(p -> p.length() > 40)
                    .distinct()
                    .collect(Collectors.joining("\n\n"));

            if (text.length() > 500) article.setContent(text);
        } catch (Exception ex) {
            System.err.println("No se pudo coger el texto de: " + article.getUrl());
        }
    }

    public NewsTotalArticles fallbackGetNewsByCategory(String category, int maxArticles, Throwable t) {
        System.err.println("Circuit Breaker activado para categoría " + category + " - Motivo: " + t.getMessage());
        return new NewsTotalArticles(0L, Collections.emptyList());
    }
}