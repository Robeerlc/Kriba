package org.kriba.news.service;


import org.kriba.news.model.NewsTotalArticles;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;

import java.util.Collections;

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
        if (response != null && response.getArticles() != null) {
            response.getArticles().forEach(article -> article.setCategory(category));
        }
        return response != null ? response : new NewsTotalArticles(0L, Collections.emptyList());
    }


        public NewsTotalArticles getGeneralFeed (){
        return null;
            //TODO generar feed general
        }
    }
