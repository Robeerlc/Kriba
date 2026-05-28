package org.kriba.news.service;

import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.kriba.analytics.repository.InteractionRepository;
import org.kriba.news.dto.FeedCache;
import org.kriba.news.dto.NewsInfo;
import org.kriba.news.dto.NewsTotalArticles;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cache.Cache;
import org.springframework.cache.CacheManager;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.context.annotation.Lazy;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class NewsInfoService {

    private static final int DEFAULT_CATEGORY_MAX_ARTICLES = 45;
    private static final int FEED_BATCH_SIZE = 100;
    private static final int MIN_ARTICLES_PER_CATEGORY = 2;

    private final String apiKey;
    private final RestClient restClient;
    private final InteractionRepository interactionRepository;
    private final CacheManager cacheManager;

    @Autowired
    @Lazy
    private NewsInfoService selfProxy;

    public NewsInfoService(@Value("${apiKey}") String apiKey, InteractionRepository interactionRepository, CacheManager cacheManager) {
        this.apiKey = apiKey;
        this.interactionRepository = interactionRepository;
        this.cacheManager = cacheManager;
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


    public Page<NewsInfo> getNewsByCategory(String category, Pageable pageable){
        NewsTotalArticles response = selfProxy.getNewsByCategory(category, DEFAULT_CATEGORY_MAX_ARTICLES);
        List<NewsInfo> articles = response.articles();
        return paginate(articles, pageable);
    }
    public NewsTotalArticles getGeneralFeed(Long userId){
        List<NewsInfo> articles = buildGeneralFeedArticles(userId, FEED_BATCH_SIZE);
        return new NewsTotalArticles((long)articles.size(), articles);
    }
    public Page<NewsInfo> getGeneralFeed(Long userId, Pageable pageable){
        int requiredEnd = (int) pageable.getOffset() + pageable.getPageSize();
        List<NewsInfo> articles = getOrExpandCachedFeed(userId, requiredEnd);
        return paginate(articles, pageable);
    }

    private List<NewsInfo> getOrExpandCachedFeed(Long userId, int requiredEnd){
        String cacheKey = buildGeneralFeedCacheKey(userId);

        List<NewsInfo> cachedArticles = getArticlesFromCache(cacheKey);

        if(cachedArticles.size() >= requiredEnd){
            return cachedArticles;
        }

        List<NewsInfo> expandedArticles = new ArrayList<>(cachedArticles);

        while(expandedArticles.size() < requiredEnd){
            List<NewsInfo> newBatch = buildGeneralFeedArticles(userId, FEED_BATCH_SIZE);
            List<NewsInfo> cleanNewBatch = removeAlreadyCachedArticles(expandedArticles, newBatch);

            if(cleanNewBatch.isEmpty()){
                break;
            }

            expandedArticles.addAll(cleanNewBatch);
            expandedArticles = cleanAndSortArticles(expandedArticles);
        }
        saveArticlesInCache(cacheKey, expandedArticles);
        return  expandedArticles;
    }

    private String buildGeneralFeedCacheKey(Long userId){
        return "user:" + userId;
    }
    private List<NewsInfo> getArticlesFromCache(String cacheKey){
        Cache cache = cacheManager.getCache("generalFeed");

        if(cache == null) return new ArrayList<>();

        FeedCache feedCache =  cache.get(cacheKey, FeedCache.class);
        if (feedCache == null || feedCache.articles() == null) {
            return new ArrayList<>();
        }
        return new ArrayList<>(feedCache.articles());
    }

    private void saveArticlesInCache(String cacheKey, List<NewsInfo> articles){
        Cache cache = cacheManager.getCache("generalFeed");

        if(cache != null){
            cache.put(cacheKey,new FeedCache(articles));
        }
    }



    public List<NewsInfo> buildGeneralFeedArticles(Long userId, int batchSize) {
        List<String> allCategories = List.of("general", "world", "nation", "business", "technology", "entertainment", "sports", "science", "health");
        int remainingArticlesToDistribute = batchSize - (allCategories.size() * MIN_ARTICLES_PER_CATEGORY);

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
            int amountToFetch = MIN_ARTICLES_PER_CATEGORY;
            if (totalPoints > 0) {
                long pointsInThisCategory = userPointsMap.getOrDefault(cat, 0L);
                double percentage = (double) pointsInThisCategory / totalPoints;
                amountToFetch += (int) Math.round(percentage * remainingArticlesToDistribute);
            } else {
                amountToFetch = batchSize / allCategories.size();
            }

            NewsTotalArticles response = selfProxy.getNewsByCategory(cat, amountToFetch);
            if (response != null) {
                if (response.totalArticles() != null) granTotalGNews += response.totalArticles();
                if (response.articles() != null) allArticles.addAll(response.articles());
            }
        }

        return cleanAndSortArticles(allArticles);
    }
    private Page<NewsInfo> paginate(List<NewsInfo> articles, Pageable pageable){
        int start = (int) pageable.getOffset();
        int end = Math.min(start + pageable.getPageSize(), articles.size());

        if(start >= articles.size()){
            return Page.empty(pageable);
        }

        List<NewsInfo> content = articles.subList(start, end);
        return new PageImpl<>(content, pageable, articles.size());
    }

    private List<NewsInfo> removeAlreadyCachedArticles(List<NewsInfo> cachedArticles, List<NewsInfo> newArticles) {

        List<String> cachedUrls = cachedArticles.stream()
                .map(NewsInfo::getUrl)
                .filter(url -> url != null && !url.isBlank())
                .toList();

        return newArticles.stream()
                .filter(article -> article.getUrl() != null && !article.getUrl().isBlank())
                .filter(article -> !cachedUrls.contains(article.getUrl()))
                .toList();
    }

    private List<NewsInfo> cleanAndSortArticles(List<NewsInfo> articles){
        if(articles == null || articles.isEmpty()){
            return Collections.emptyList();
        }

        return articles.stream().filter(article -> article.getUrl() != null && !article.getUrl().isBlank())
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
                .toList();
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