package org.kriba.news.controller;

import jakarta.validation.Valid;
import org.kriba.news.dto.FeedRequest;
import org.kriba.news.dto.NewsInfo;
import org.kriba.news.service.NewsInfoService;
import org.kriba.users.service.UserService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
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

    public ResponseEntity<Page<NewsInfo>> getFeed(@Valid @RequestBody FeedRequest request,
                                                  @PageableDefault(size = 10,
                                                  sort = "publishedAt", direction = Sort.Direction.DESC) Pageable pageable) {
        Long currentUserId = null;
        if (request.loginRequest() != null)
            currentUserId = userService.login(request.loginRequest()).userId();
        return ResponseEntity.ok(newsService.getFeed(currentUserId,request.category(),pageable));
    }
}