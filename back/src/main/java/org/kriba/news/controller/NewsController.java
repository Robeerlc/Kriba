package org.kriba.news.controller;

import org.kriba.news.model.NewsTotalArticles;
import org.kriba.news.service.NewsServiceInfo;
import org.springframework.web.bind.annotation.*;


@RestController
@RequestMapping(path = "/api")
public class NewsController {

    private final NewsServiceInfo newsService;

    public NewsController(NewsServiceInfo newsService) {
        this.newsService = newsService;
    }

    @GetMapping(path = "/feed")
    public NewsTotalArticles getFeed(
            @RequestHeader("userId") Long userId,
            @RequestParam(name = "category", required = false) String category) {
        if (category != null && !category.isEmpty()) {
            return newsService.getNewsByCategory(category);
        } else {
            return newsService.getGeneralFeed();
        }

    }
}

