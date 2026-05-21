package org.kriba.subscriptions.service;

import org.kriba.subscriptions.model.Subscription;
import org.kriba.subscriptions.repository.SubscriptionRepository;
import org.kriba.users.model.User;
import org.kriba.users.repository.UserRepository;
import org.springframework.stereotype.Service;

@Service
public class SubscriptionsServiceInfo {

	private final UserRepository userRepository;
	private final SubscriptionRepository subscriptionRepository;

	public SubscriptionsServiceInfo(UserRepository userRepository, SubscriptionRepository subscriptionRepository) {
		this.userRepository = userRepository;
		this.subscriptionRepository = subscriptionRepository;
	}

	public void subscribe(long userId, String externalSourceId, String sourceName) {
		User user = userRepository.findById(userId)
				.orElseThrow(() -> new IllegalArgumentException("Usuario no encontrado"));

		Subscription subscription = Subscription.builder().userId(user.getId()).externalSourceId(externalSourceId)
				.sourceName(sourceName).build();
		subscriptionRepository.save(subscription);
	}

}
