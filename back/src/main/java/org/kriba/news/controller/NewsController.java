package org.kriba.news.controller;

import jakarta.validation.Valid;
import org.kriba.news.dto.FeedRequest;
import org.kriba.news.dto.NewsTotalArticles;
import org.kriba.news.service.NewsInfoService;
import org.kriba.users.service.UserService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;


@RestController
@RequestMapping(path = "/api/v1/feed")
public class NewsController {

    private final NewsInfoService newsService;
    private final UserService userService;

    public NewsController(NewsInfoService newsService, UserService userService) {
        this.newsService = newsService;
        this.userService = userService;
    }

    @PostMapping
    public ResponseEntity<NewsTotalArticles> getFeed(@Valid @RequestBody FeedRequest request) {
        Long currentUserId = null;
        if (request.loginRequest() != null)
            currentUserId = userService.login(request.loginRequest()).userId();

        String category = request.category();
        if (category != null && !category.isBlank())
            return ResponseEntity.ok(newsService.getNewsByCategory(category, 20));

        return ResponseEntity.ok(newsService.getGeneralFeed(currentUserId));
    }
}