package org.kriba.bookmarks.controller;


import org.kriba.bookmarks.dto.SaveRequest;
import org.kriba.bookmarks.dto.SavedArticleInfo;
import org.kriba.bookmarks.service.SaveNewsService;
import org.kriba.users.dto.AuthResponse;
import org.kriba.users.dto.LoginRequest;
import org.kriba.users.service.UserService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/bookmarks")
public class SaveNewsController {

    private final SaveNewsService saveNewsService;
    private final UserService userService;

    public  SaveNewsController(SaveNewsService saveNewsService, UserService userService){
        this.saveNewsService = saveNewsService;
        this.userService = userService;
    }

    @PostMapping()
    public ResponseEntity<Void> saveNew(@RequestBody SaveRequest request){
        AuthResponse authResponse = userService.login(request.loginRequest());
        saveNewsService.saveNew(authResponse.userId(),request);

        return ResponseEntity.status(HttpStatus.CREATED).build();


    }

    @PostMapping("/list")
    public ResponseEntity<List<SavedArticleInfo>> getSavedNews(@RequestBody LoginRequest request){
        AuthResponse authResponse = userService.login(request);
        return ResponseEntity.ok(saveNewsService.getSavedNews(authResponse.userId()));
    }

}
