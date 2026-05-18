package org.kriba.news.service;


import org.kriba.news.model.NewsInfo;
import org.kriba.news.model.NewsTotalArticles;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

@Service
public class NewsServiceInfo {
    private final RestClient restClient;
    @Value("${apiKey}")
    private String apiKey;

    public NewsServiceInfo(RestClient restClient) {
        this.restClient = restClient;
    }


    public NewsTotalArticles getNewsByCategory(String category) {
        NewsTotalArticles response = restClient.get()
                .uri("https://gnews.io/api/v4/top-headlines?lang=es&country=es&category=" + category + "&apikey=" + apiKey)
                .retrieve()
                .body(NewsTotalArticles.class);
        if (response != null && response.articles() != null) {
            response.articles().forEach(article -> article.setCategory(category));
        }
        return response != null ? response : new NewsTotalArticles(0L, Collections.emptyList());
    }


    public NewsTotalArticles getGeneralFeed() {
        List<String> categories = List.of("general", "world", "nation", "business", "technology", "entertainment", "sports", "science", "health");
        List<NewsInfo> allArticles = new ArrayList<>();

        for (String cat : categories) {
            try {
                NewsTotalArticles response = getNewsByCategory(cat);
                if (response != null && response.articles() != null) allArticles.addAll(response.articles());
                Thread.sleep(1000);
            } catch (Exception e) {
                System.err.println("Error al cargar la categoría " + cat + ": " + e.getMessage());
            }
        }

        Collections.shuffle(allArticles);
        return new NewsTotalArticles((long) allArticles.size(), allArticles);
    }
}
