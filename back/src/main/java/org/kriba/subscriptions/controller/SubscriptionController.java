package org.kriba.subscriptions.controller;

import org.kriba.subscriptions.service.SubscriptionsServiceInfo;
import org.kriba.users.dto.LoginRequest;
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
    
	protected SubscriptionController(SubscriptionsServiceInfo subscriptionService) {
		this.subscriptionService = subscriptionService;
	}
	
	@PostMapping("/subscribe")
	public ResponseEntity<Void> subscribe(@RequestBody LoginRequest request, String externalSourceId,String sourceName){
		subscriptionService.subscribe(request.email(), externalSourceId, sourceName);
		return ResponseEntity.status(HttpStatus.CREATED).build();
	}
	
	
	
	

	
}
