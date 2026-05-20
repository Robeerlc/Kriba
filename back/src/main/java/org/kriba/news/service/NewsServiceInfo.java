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

    private final String apiKey;
    private final RestClient restClient;

    public NewsServiceInfo(@Value("${apiKey}") String apiKey) {
        this.apiKey = apiKey;
        this.restClient = RestClient.builder()
                .baseUrl("https://gnews.io/api/v4")
                .build();
    }

    public NewsTotalArticles getNewsByCategory(String category) {
        NewsTotalArticles response = restClient.get()
                .uri("/top-headlines?lang=es&country=es&category=" + category + "&apikey=" + apiKey)
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
                if (response != null && response.articles() != null) {
                    allArticles.addAll(response.articles());
                }
                Thread.sleep(1000);
            } catch (InterruptedException ex) {
                System.err.println("Error al cargar la categoría " + cat + ": " + ex.getMessage());
            }
        }

        Collections.shuffle(allArticles);
        return new NewsTotalArticles((long) allArticles.size(), allArticles);
    }
}