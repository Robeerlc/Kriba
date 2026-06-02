package org.kriba.users.controller;

import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Value;
import org.kriba.users.dto.*;
import org.kriba.users.service.UserService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/auth")
public class AuthController {

    private final UserService userService;

    public AuthController(UserService userService) {
        this.userService = userService;
    }

    @PostMapping("/register")
    public ResponseEntity<AuthResponse> register(@Valid @RequestBody RegisterRequest request) {
        return ResponseEntity.ok(userService.register(request));
    }

    @PostMapping("/login")
    public ResponseEntity<AuthResponse> login(@Valid @RequestBody LoginRequest request) {
        return ResponseEntity.ok(userService.login(request));
    }

    @PostMapping("/update")
    public ResponseEntity<AuthResponse> modifyData(@Valid @RequestBody ModifyRequest request) {
        return ResponseEntity.ok(userService.modifyData(request));
    }

    @PostMapping("/delete")
    public ResponseEntity<Void> deleteAccount(@Valid @RequestBody LoginRequest request) {
        userService.deleteAccount(request);
        return ResponseEntity.noContent().build();
    }

    @Value("${frontend.base-url}")
    private String frontendBaseUrl;

    @GetMapping("/verify/{uuid}")
    public ResponseEntity<Void> verify(@PathVariable UUID uuid) {

        userService.verifyAccount(uuid);

        HttpHeaders headers = new HttpHeaders();
        headers.add("Location", frontendBaseUrl + "/login");

        return new ResponseEntity<>(headers, HttpStatus.FOUND);
    }
}