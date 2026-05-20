package org.kriba.bookmarks.controller;


import org.kriba.bookmarks.dto.SaveRequest;
import org.kriba.bookmarks.dto.SavedArticleInfo;
import org.kriba.bookmarks.service.SaveNewsService;
import org.kriba.users.dto.LoginRequest;
import org.kriba.users.service.UserService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/bookmarks")
public class SaveNewsController {

    private final SaveNewsService saveNewsService;
    private final UserService userService;

    public  SaveNewsController(SaveNewsService saveNewsService, UserService userService){
        this.saveNewsService = saveNewsService;
        this.userService = userService;
    }

    @PostMapping("/v1")
    public ResponseEntity<Void> saveNew(@RequestBody SaveRequest request){
        if (request == null || request.loginRequest() == null || request.savedNew() == null) {
            return ResponseEntity.badRequest().build();
        }
        userService.login(request.loginRequest());
        saveNewsService.saveNew(request);
        return ResponseEntity.status(HttpStatus.CREATED).build();


    }

    @PostMapping("/list/v1")
    public ResponseEntity<List<SavedArticleInfo>> getSavedNews(@RequestBody LoginRequest request){
        if(request == null)
            return ResponseEntity.badRequest().build();

        userService.login(request);

        return ResponseEntity.ok(saveNewsService.getSavedNews(request));
    }

}
