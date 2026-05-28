package org.kriba.bookmarks.controller;

import jakarta.validation.Valid;
import org.kriba.bookmarks.dto.ArticleResponse;
import org.kriba.bookmarks.dto.SaveRequest;
import org.kriba.bookmarks.dto.UnsaveRequest;
import org.kriba.bookmarks.service.SaveNewsService;
import org.kriba.users.dto.AuthResponse;
import org.kriba.users.dto.LoginRequest;
import org.kriba.users.service.UserService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;


@RestController
@RequestMapping("/api/v1/bookmarks")
public class SaveNewsController {

    private final SaveNewsService saveNewsService;
    private final UserService userService;

    public SaveNewsController(SaveNewsService saveNewsService, UserService userService) {
        this.saveNewsService = saveNewsService;
        this.userService = userService;
    }

    @PostMapping
    public ResponseEntity<Void> saveNew(@Valid @RequestBody SaveRequest request) {
        AuthResponse authResponse = userService.login(request.loginRequest());
        saveNewsService.saveNew(authResponse.userId(), request.savedNew());

        return ResponseEntity.status(HttpStatus.CREATED).build();
    }

    @PostMapping("/list")
    public ResponseEntity<Page<ArticleResponse>> getSavedNews(@Valid @RequestBody LoginRequest request,
                                                              @RequestParam(defaultValue = "0")int pageNo,
                                                              @RequestParam(defaultValue = "10")int pageSize) {
        AuthResponse authResponse = userService.login(request);
        Pageable pageable = PageRequest.of(pageNo, pageSize);
        return ResponseEntity.ok(saveNewsService.getSavedNews(authResponse.userId(), pageable));
    }

    @PostMapping("/unsave")
    public ResponseEntity<Void> unsave(@Valid @RequestBody UnsaveRequest request) {
        AuthResponse authResponse = userService.login(request.loginRequest());
        saveNewsService.unsave(authResponse.userId(), request.externalArticleId());
        return ResponseEntity.noContent().build();
    }
}
