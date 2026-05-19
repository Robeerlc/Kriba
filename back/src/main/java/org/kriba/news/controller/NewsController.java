package org.kriba.news.controller;

import org.kriba.news.dto.FeedRequest;
import org.kriba.news.model.NewsTotalArticles;
import org.kriba.news.service.NewsServiceInfo;
import org.kriba.users.service.UserService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;


@RestController
@RequestMapping(path = "/api")
public class NewsController {

    private final NewsServiceInfo newsService;
    private final UserService userService;

    public NewsController(NewsServiceInfo newsService, UserService userService) {
        this.newsService = newsService;
        this.userService = userService;
    }

    @PostMapping(path = "/feed")
    public ResponseEntity<NewsTotalArticles> getFeed(@RequestBody FeedRequest request) {
    	if (request != null && request.loginRequest() != null) 
    		userService.login(request.loginRequest());

        String category = request != null ? request.category() : null;
        if (category != null && !category.isEmpty()) 
        	return ResponseEntity.ok(newsService.getNewsByCategory(category));

        return ResponseEntity.ok(newsService.getGeneralFeed());
    }
}