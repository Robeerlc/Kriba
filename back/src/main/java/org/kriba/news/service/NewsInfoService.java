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

    private static final int DEFAULT_CATEGORY_MAX_ARTICLES = 10
            ;
    private static final int FEED_BATCH_SIZE = 20;
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

    public Page<NewsInfo> refreshFeed(Long userId, String category, Pageable pageable){
        String cacheKey = buildFeedCacheKey(userId,category);

        FeedCache feedCache = getFeedCache(cacheKey);
        List<NewsInfo> cachedArticles = new ArrayList<>(feedCache.articles());
        if(cachedArticles.isEmpty()){
            List<NewsInfo> initialBatch = cleanAndSortArticles(loadRecentBatch(userId, category, FEED_BATCH_SIZE));
            saveFeedCache(cacheKey,initialBatch);
            return paginate(initialBatch, pageable);
        }

        List<NewsInfo> newBatch = loadNewerBatch(userId,category,FEED_BATCH_SIZE, feedCache.newestPublishedAt());
        if(!newBatch.isEmpty()){
            return Page.empty(pageable);
        }
        cachedArticles.addAll(newBatch);
        cachedArticles = cleanAndSortArticles(cachedArticles);
        saveFeedCache(cacheKey,cachedArticles);

        return paginate(cleanAndSortArticles(cachedArticles), pageable);
    }

    public Page<NewsInfo> scrollFeed(Long userId,String category,Pageable pageable){
        String cacheKey = buildFeedCacheKey(userId,category);

        FeedCache feedCache = getFeedCache(cacheKey);
        List<NewsInfo> articles = new ArrayList<>(feedCache.articles());
        int pageSize = pageable.getPageSize();
        int start;


        if(pageable.getPageNumber() == 0 && feedCache.lastDeliveredIndex() != null){
            start = feedCache.lastDeliveredIndex() + 1;
        }else{
            start = (int) pageable.getOffset();
        }
        int requiredEnd = start + pageSize;
        articles = getOrExpandCachedFeed(userId, category,cacheKey,requiredEnd);

        if(articles.isEmpty() || start >= articles.size()){
            saveFeedCache(cacheKey, articles, feedCache.lastDeliveredIndex());
            return Page.empty(pageable);
        }

        int end = Math.min(requiredEnd, articles.size());
        List<NewsInfo> content = articles.subList(start,end);
        int newLastDeliveredIndex = end - 1;
        saveFeedCache(cacheKey,articles,newLastDeliveredIndex);

        return new PageImpl<>(content,pageable,articles.size());
    }
    public List<NewsInfo> getOrExpandCachedFeed(Long userId, String category,String cacheKey,int requiredEnd) {

    FeedCache feedCache = getFeedCache(cacheKey);

    List<NewsInfo> expandedArticles =
            new ArrayList<>(feedCache.articles());

    if (expandedArticles.size() >= requiredEnd) {
        return expandedArticles;
    }

    String oldestPublishedAt = feedCache.oldestPublishedAt();

    while (expandedArticles.size() < requiredEnd) {

        List<NewsInfo> newBatch = expandedArticles.isEmpty()
                ? loadRecentBatch(userId, category, FEED_BATCH_SIZE)
                : loadOlderBatch(userId, category, FEED_BATCH_SIZE, oldestPublishedAt);

        List<NewsInfo> cleanNewBatch =
                removeAlreadyCachedArticles(expandedArticles, newBatch);

        if (!cleanNewBatch.isEmpty()) {

            expandedArticles.addAll(cleanNewBatch);

            expandedArticles =
                    new ArrayList<>(cleanAndSortArticles(expandedArticles));

            oldestPublishedAt =
                    findOldestPublishedAt(expandedArticles);

        } else {

            requiredEnd = expandedArticles.size();
        }
    }

    saveFeedCache(cacheKey, expandedArticles);
    return expandedArticles;
}
    public List<NewsInfo> loadRecentBatch(Long userId, String category, int batchSize){
        return buildFeedArticles(userId,category,batchSize,null,null);
    }
    public List<NewsInfo> loadOlderBatch(Long userid, String category, int batchSize, String oldestPublishedAt){
        return buildFeedArticles(userid, category,batchSize,null,oldestPublishedAt);
    }
    public List<NewsInfo> loadNewerBatch(Long userId, String category, int batchSize, String newestPublishedAt){
        return buildFeedArticles(userId,category,batchSize,newestPublishedAt,null);
    }

    public List<NewsInfo> buildFeedArticles(Long userId,String category, int batchSize, String fromDate, String toDate){
        if(category != null && !category.isBlank()){
            int maxArticles = Math.min(batchSize, DEFAULT_CATEGORY_MAX_ARTICLES);
            String query = category;
            NewsTotalArticles response = selfProxy.searchNewsFromGnews(query,maxArticles,fromDate,toDate,category);

            if(response == null || response.articles() == null){
                return Collections.emptyList();
            }

            return cleanAndSortArticles(response.articles());
        }

        return buildGeneralFeedArticles(userId, batchSize, fromDate, toDate);

    }

    public List<NewsInfo> buildGeneralFeedArticles(Long userId, int batchSize, String fromDate, String toDate) {
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

            NewsTotalArticles response = selfProxy.searchNewsFromGnews(cat, amountToFetch,fromDate,toDate,cat);
            if (response != null) {
                if (response.totalArticles() != null) granTotalGNews += response.totalArticles();
                if (response.articles() != null) allArticles.addAll(response.articles());
            }
        }

        return cleanAndSortArticles(allArticles);
    }

    @CircuitBreaker(name= "gnewsApi", fallbackMethod = "fallBackGetNewsByCategory")
    public NewsTotalArticles searchNewsFromGnews(String query,int maxArticles, String fromDate,String toDate,String category){
        UriComponentsBuilder uriBuilder = UriComponentsBuilder
                .fromPath("/search")
                .queryParam("q",query)
                .queryParam("lang","es")
                .queryParam("country","es")
                .queryParam("max",maxArticles)
                .queryParam("sortby","publishedAt")
                .queryParam("apikey",apiKey);

        if(fromDate != null && !fromDate.isBlank()){
            uriBuilder.queryParam("from",fromDate);
        }

        if(toDate != null && !toDate.isBlank()){
            uriBuilder.queryParam("to",toDate);
        }

        NewsTotalArticles response = restClient.get()
                .uri(uriBuilder.toUriString())
                .retrieve()
                .body(NewsTotalArticles.class);

        try {
            Thread.sleep(1000);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }

        if(response != null && response.articles() != null){
            response.articles().forEach(article -> article.setCategory(category));
            response.articles().parallelStream().forEach(this::scrapFullText);
        }

        return response != null ? response : new NewsTotalArticles(0L, Collections.emptyList());
    }

    public String buildFeedCacheKey(Long userId, String category){
        String user = "user:";
        if(userId != null) user = user + userId;

        if(category == null || category.isBlank()){
            return user + ":general";
        }
        return user + ":category:" + category;
    }

    public FeedCache getFeedCache(String cacheKey){
        Cache cache = cacheManager.getCache("generalFeed");

        if(cache == null){
            return new FeedCache(new ArrayList<>(), null, null,null);
        }
        FeedCache feedCache = cache.get(cacheKey, FeedCache.class);
        if(feedCache == null || feedCache.articles() == null){
            return new FeedCache(new ArrayList<>(),null,null,null);
        }

        return new FeedCache(new ArrayList<>(feedCache.articles()),feedCache.newestPublishedAt(),feedCache.oldestPublishedAt(),feedCache.lastDeliveredIndex());
    }

    public void saveFeedCache(String cacheKey, List<NewsInfo> articles){
        FeedCache oldCache = getFeedCache(cacheKey);
        saveFeedCache(cacheKey, articles, oldCache.lastDeliveredIndex());
    }


    public void saveFeedCache(String cacheKey,List<NewsInfo> articles, Integer lastDeliveredIndex){
        Cache cache = cacheManager.getCache("generalFeed");
        if(cache != null){
            List<NewsInfo> savedArticles = cleanAndSortArticles(articles);
            FeedCache feedCache = new FeedCache(new ArrayList<>(savedArticles),findNewestPublishedAt(savedArticles), findOldestPublishedAt(savedArticles),lastDeliveredIndex);
            cache.put(cacheKey, feedCache);
        }

    }
    public List<NewsInfo> removeAlreadyCachedArticles(List<NewsInfo> cachedArticles, List<NewsInfo> newArticles) {

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





    private Page<NewsInfo> paginate(List<NewsInfo> articles, Pageable pageable){
        int start = (int) pageable.getOffset();
        int end = Math.min(start + pageable.getPageSize(), articles.size());

        if(start >= articles.size()){
            return Page.empty(pageable);
        }

        List<NewsInfo> content = articles.subList(start, end);
        return new PageImpl<>(content, pageable, articles.size());
    }

    private String findNewestPublishedAt(List<NewsInfo> articles) {
        if (articles == null || articles.isEmpty()) {
            return null;
        }

        return articles.stream()
                .map(NewsInfo::getPublishedAt)
                .filter(x-> x != null && !x.isBlank())
                .max(String::compareTo)
                .orElse(null);
    }

    private String findOldestPublishedAt(List<NewsInfo> articles) {
        if (articles == null || articles.isEmpty()) {
            return null;
        }

        return articles.stream()
                .map(NewsInfo::getPublishedAt)
                .filter(x -> x != null && !x.isBlank())
                .min(String::compareTo)
                .orElse(null);
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

    public NewsTotalArticles fallBackGetNewsByCategory(String query, int maxArticles,String fromDate, String toDate, String category, Throwable t) {
        System.err.println("Circuit Breaker activado para categoría " + category + " - Motivo: " + t.getMessage());
        return new NewsTotalArticles(0L, Collections.emptyList());
    }
}
