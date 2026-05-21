package org.kriba.bookmarks.controller;

import org.kriba.bookmarks.dto.ArticleResponseDto;
import org.kriba.bookmarks.dto.SaveRequest;
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
    public ResponseEntity<Void> saveNew(@RequestBody SaveRequest request) {
        if (request == null || request.loginRequest() == null || request.savedNew() == null) {
            return ResponseEntity.badRequest().build();
        }
        AuthResponse authResponse = userService.login(request.loginRequest());
        saveNewsService.saveNew(authResponse.userId(), request.savedNew());

        return ResponseEntity.status(HttpStatus.CREATED).build();
    }

    @PostMapping("/list")
    public ResponseEntity<List<ArticleResponseDto>> getSavedNews(@RequestBody LoginRequest request) {
        if(request == null) return ResponseEntity.badRequest().build();

        AuthResponse authResponse = userService.login(request);
        return ResponseEntity.ok(saveNewsService.getSavedNews(authResponse.userId()));
    }
}
