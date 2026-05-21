package org.kriba.summarize.controller;

import org.kriba.summarize.dto.SummarizeOutDTO;
import org.kriba.summarize.dto.SummarizeRequest;
import org.kriba.summarize.service.SummarizeService;
import org.kriba.users.repository.UserRepository;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/ai/summarize")
public class SummarizeController {
    private final SummarizeService summarizeService;
    private final UserRepository userRepository;

    public SummarizeController(SummarizeService summarizeService, UserRepository userRepository) {
        this.summarizeService = summarizeService;
        this.userRepository = userRepository;
    }

    @PostMapping()
    public ResponseEntity<SummarizeOutDTO> summarize(@RequestBody SummarizeRequest request){
        if (request == null || request.loginRequest() == null || request.textContent() == null) {
            return ResponseEntity.badRequest().build();
        }
        Long currentUserId = userRepository.findByEmail(request.loginRequest().email()).orElseThrow(() -> new IllegalArgumentException("Ese usuario no existe")).getId();
        return ResponseEntity.ok(summarizeService.summarize(currentUserId, request.textContent(),request.articleUrl()));
    }
}



