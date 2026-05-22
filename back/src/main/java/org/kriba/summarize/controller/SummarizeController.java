package org.kriba.summarize.controller;

import org.kriba.summarize.dto.SummarizeRequest;
import org.kriba.summarize.dto.SummarizeResponse;
import org.kriba.summarize.service.SummarizeService;
import org.kriba.users.dto.AuthResponse;
import org.kriba.users.service.UserService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/ai/summarize")
public class SummarizeController {
    private final SummarizeService summarizeService;
    private final UserService userService;

    public SummarizeController(SummarizeService summarizeService, UserService userService) {
        this.summarizeService = summarizeService;
        this.userService = userService;
    }

    @PostMapping()
    public ResponseEntity<SummarizeResponse> summarize(@RequestBody SummarizeRequest request) {
        if (request == null || request.loginRequest() == null || request.textContent() == null) {
            return ResponseEntity.badRequest().build();
        }
        AuthResponse authResponse = userService.login(request.loginRequest());
        return ResponseEntity.ok(summarizeService.summarize(authResponse.userId(), request.textContent(), request.articleUrl(), request.category()));
    }
}