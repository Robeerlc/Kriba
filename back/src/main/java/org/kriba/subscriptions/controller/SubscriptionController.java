package org.kriba.subscriptions.controller;

import org.kriba.subscriptions.dto.AuthSubscription;
import org.kriba.subscriptions.dto.SubscriptionList;
import org.kriba.subscriptions.service.SubscriptionsService;
import org.kriba.users.dto.AuthResponse;
import org.kriba.users.dto.LoginRequest;
import org.kriba.users.service.UserService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping(path = "/api/v1/subscriptions")
public class SubscriptionController {

    private final SubscriptionsService subscriptionService;
    private final UserService userService;

    public SubscriptionController(SubscriptionsService subscriptionService, UserService userService) {
        this.subscriptionService = subscriptionService;
        this.userService = userService;
    }

    @PostMapping
    public ResponseEntity<Void> subscribe(@RequestBody AuthSubscription request) {
        if (request == null || request.loginRequest() == null) {
            return ResponseEntity.badRequest().build();
        }
        AuthResponse authResponse = userService.login(request.loginRequest());
        subscriptionService.subscribe(authResponse.userId(), request.externalSourceId(), request.sourceName());
        return ResponseEntity.status(HttpStatus.CREATED).build();
    }

    @PostMapping("/unsubscribe")
    public ResponseEntity<Void> unsubscribe(@RequestBody AuthSubscription request){
        if (request == null || request.loginRequest() == null) {
            return ResponseEntity.badRequest().build();
        }
        AuthResponse authResponse = userService.login(request.loginRequest());
        subscriptionService.unsubscribe(authResponse.userId(), request.externalSourceId(), request.sourceName());
        return ResponseEntity.status(HttpStatus.NON_AUTHORITATIVE_INFORMATION).build();
    }


    @PostMapping("/list")
    public ResponseEntity<SubscriptionList> listSubscriptions(@RequestBody LoginRequest request) {
        if (request == null) {
            return ResponseEntity.badRequest().build();
        }
        AuthResponse authResponse = userService.login(request);
        return ResponseEntity.ok(subscriptionService.getSubscriptions(authResponse.userId()));
    }
}