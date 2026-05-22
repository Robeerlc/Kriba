package org.kriba.analytics.controller;

import org.kriba.analytics.dto.InteractionRequest;
import org.kriba.analytics.model.Interaction;
import org.kriba.analytics.repository.InteractionRepository;
import org.kriba.users.dto.AuthResponse;
import org.kriba.users.service.UserService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1")
public class InteractionController {

    private final InteractionRepository interactionRepository;
    private final UserService userService;

    public InteractionController(InteractionRepository interactionRepository, UserService userService) {
        this.interactionRepository = interactionRepository;
        this.userService = userService;
    }

    @PostMapping("/interactions")
    public ResponseEntity<Void> saveInteraction(@RequestBody InteractionRequest request) {
        AuthResponse authUser = userService.login(request.loginRequest());

        Interaction interaction = Interaction.builder()
                .userId(authUser.userId())
                .articleCategory(request.articleCategory())
                .interactionType(request.interactionType())
                .build();
        interactionRepository.save(interaction);
        return ResponseEntity.ok().build();
    }
}