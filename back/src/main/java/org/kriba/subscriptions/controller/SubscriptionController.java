package org.kriba.subscriptions.controller;

import org.kriba.subscriptions.dto.AuthSubscription;
import org.kriba.subscriptions.service.SubscriptionsServiceInfo;
import org.kriba.users.dto.AuthResponse;
import org.kriba.users.service.UserService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping(path = "/api/v1/subscriptions")
public class SubscriptionController {
	
	private final SubscriptionsServiceInfo subscriptionService;
    private final UserService userService;
	
	
	public SubscriptionController(SubscriptionsServiceInfo subscriptionService, UserService userService) {
		this.subscriptionService = subscriptionService;
		this.userService = userService;
	}


	@PostMapping()
	public ResponseEntity<Void> subscribe(@RequestBody AuthSubscription request){
		if (request == null || request.loginRequest() == null) {
		    return ResponseEntity.badRequest().build();
		}
		AuthResponse authResponse = userService.login(request.loginRequest());
		subscriptionService.subscribe(authResponse.userId(), request.externalSourceId(), request.sourceName());
		return ResponseEntity.status(HttpStatus.CREATED).build();
	}
	
	
	
	

	
}
