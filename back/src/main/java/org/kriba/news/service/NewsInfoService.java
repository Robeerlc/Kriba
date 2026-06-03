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
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;
import org.springframework.web.util.UriComponentsBuilder;

import java.util.*;
import java.util.stream.Collectors;

@Service
public class NewsInfoService {

    private static final int DEFAULT_CATEGORY_MAX_ARTICLES = 10;
    private static final int MIN_ARTICLES_PER_CATEGORY = 2;

    private final String apiKey;
    private final RestClient restClient;
    private final InteractionRepository interactionRepository;

    @Autowired
    @Lazy
    private NewsInfoService selfProxy;

    public NewsInfoService(
            @Value("${apiKey}") String apiKey,
            InteractionRepository interactionRepository
    ) {
        this.apiKey = apiKey;
        this.interactionRepository = interactionRepository;
        this.restClient = RestClient.builder()
                .baseUrl("https://gnews.io/api/v4")
                .build();
    }

    public Page<NewsInfo> getFeed(Long userId, String category, Pageable pageable) {
        int gnewsPage = pageable.getPageNumber() + 1;
        int pageSize = Math.min(pageable.getPageSize(), DEFAULT_CATEGORY_MAX_ARTICLES);

        if (category != null && !category.isBlank()) {
            NewsTotalArticles response = selfProxy.searchNewsFromGnews(
                    category,
                    pageSize,
                    gnewsPage,
                    category
            );

            List<NewsInfo> articles = (response != null && response.articles() != null)
                    ? cleanAndSortArticles(response.articles())
                    : Collections.emptyList();

            long totalArticles = (response != null && response.totalArticles() != null)
                    ? response.totalArticles()
                    : articles.size();

            return new PageImpl<>(articles, pageable, totalArticles);
        }

        List<NewsInfo> articles = buildGeneralFeedArticles(
                userId,
                pageSize,
                gnewsPage
        );

        return new PageImpl<>(articles, pageable, articles.size());
    }

    public List<NewsInfo> buildGeneralFeedArticles(Long userId, int pageSize, int gnewsPage) {
        List<String> allCategories = List.of(
                "general",
                "world",
                "nation",
                "business",
                "technology",
                "entertainment",
                "sports",
                "science",
                "health"
        );

        int minimumNeeded = allCategories.size() * MIN_ARTICLES_PER_CATEGORY;
        int totalArticlesToFetch = Math.max(pageSize, minimumNeeded);
        int remainingArticlesToDistribute = totalArticlesToFetch - minimumNeeded;

        List<NewsInfo> allArticles = new ArrayList<>();

        long totalPoints = 0;
        Map<String, Long> userPointsMap = new HashMap<>();

        if (userId != null) {
            List<InteractionRepository.CategoryStats> interactions =
                    interactionRepository.getCategoryStatsByUserId(userId);

            for (InteractionRepository.CategoryStats stat : interactions) {
                String cat = stat.getCategory();
                long points = stat.getPoints();

                userPointsMap.put(cat, points);
                totalPoints += points;
            }
        }

        for (String cat : allCategories) {
            int amountToFetch = MIN_ARTICLES_PER_CATEGORY;

            if (totalPoints > 0) {
                long pointsInThisCategory = userPointsMap.getOrDefault(cat, 0L);
                double percentage = (double) pointsInThisCategory / totalPoints;

                amountToFetch += (int) Math.round(percentage * remainingArticlesToDistribute);
            }

            NewsTotalArticles response = selfProxy.searchNewsFromGnews(cat, amountToFetch, gnewsPage, cat
            );

            if (response != null && response.articles() != null) {
                allArticles.addAll(response.articles());
            }
        }

        return cleanAndSortArticles(allArticles)
                .stream()
                .limit(pageSize)
                .toList();
    }
    @Cacheable(value = "newsByCategory", key = "{#category, #page, #maxArticles}")
    @CircuitBreaker(name = "gnewsApi", fallbackMethod = "fallBackGetNewsByCategory")
    public NewsTotalArticles searchNewsFromGnews(String query, int maxArticles, int page, String category) {
        UriComponentsBuilder uriBuilder = UriComponentsBuilder
                .fromPath("/search")
                .queryParam("q", query)
                .queryParam("lang", "es")
                .queryParam("country", "es")
                .queryParam("max", maxArticles)
                .queryParam("page", page)
                .queryParam("sortby", "publishedAt")
                .queryParam("apikey", apiKey);

        NewsTotalArticles response = restClient.get()
                .uri(uriBuilder.toUriString())
                .retrieve()
                .body(NewsTotalArticles.class);

        if (response != null && response.articles() != null) {
            response.articles().forEach(article -> article.setCategory(category));
            response.articles().parallelStream().forEach(this::scrapFullText);
        }

        return response != null
                ? response
                : new NewsTotalArticles(0L, Collections.emptyList());
    }

    private List<NewsInfo> cleanAndSortArticles(List<NewsInfo> articles) {
        if (articles == null || articles.isEmpty()) {
            return Collections.emptyList();
        }

        return articles.stream()
                .filter(article -> article.getUrl() != null && !article.getUrl().isBlank())
                .filter(article -> article.getTitle() != null && !article.getTitle().isBlank())
                .collect(Collectors.toMap(
                        NewsInfo::getUrl,
                        article -> article,
                        (existing, duplicate) -> existing,
                        LinkedHashMap::new
                ))
                .values()
                .stream()
                .sorted(Comparator.comparing(
                        NewsInfo::getPublishedAt,
                        Comparator.nullsLast(Comparator.reverseOrder())
                ))
                .collect(Collectors.toCollection(ArrayList::new));
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

            if (text.length() > 500) {
                article.setContent(text);
            }
        } catch (Exception ex) {
            System.err.println("No se pudo coger el texto de: " + article.getUrl());
        }
    }

    public NewsTotalArticles fallBackGetNewsByCategory(String query, int maxArticles, int page, String category, Throwable throwable) {
        System.err.println("Circuit Breaker activado para categoría " + category + " - Motivo: " + throwable.getMessage()
        );

        return new NewsTotalArticles(0L, Collections.emptyList());
    }
}