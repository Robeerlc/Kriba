package org.kriba.users.controller;

import jakarta.validation.Valid;
import org.kriba.users.dto.*;
import org.kriba.users.service.UserService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

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

    @GetMapping("/verify/{id}")
    public ResponseEntity<String> verify(@PathVariable("id") Long id) {
        userService.verifyAccount(id);
        return ResponseEntity.ok("Cuenta verificada correctamente");
    }
}