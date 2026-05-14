package org.kriba.service;


import org.kriba.model.NewsTotalArticles;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;

@Service
public class NewsServiceInfo {
    private final RestClient restClient;
    @Value("${apiKey}")
    private String apiKey;

    public NewsServiceInfo(RestClient restClient) {
        this.restClient = restClient;
    }


    public NewsTotalArticles getNewsInfo(){
        return restClient.get()
                .uri("https://gnews.io/api/v4/top-headlines?lang=es&country=es&apikey=" + apiKey)
                .retrieve()
                .body(NewsTotalArticles.class);
    }


}
