package org.kriba.subscriptions.service;

import java.util.List;

import org.kriba.subscriptions.dto.SubscriptionList;
import org.kriba.subscriptions.dto.SubscriptionResponse;
import org.kriba.subscriptions.model.Subscription;
import org.kriba.subscriptions.repository.SubscriptionRepository;
import org.kriba.users.model.User;
import org.kriba.users.repository.UserRepository;
import org.springframework.stereotype.Service;

@Service
public class SubscriptionsService {

	private final UserRepository userRepository;
	private final SubscriptionRepository subscriptionRepository;

	public SubscriptionsService(UserRepository userRepository, SubscriptionRepository subscriptionRepository) {
		this.userRepository = userRepository;
		this.subscriptionRepository = subscriptionRepository;
	}

	public void subscribe(long userId, String externalSourceId, String sourceName) {
		User user = userRepository.findById(userId)
				.orElseThrow(() -> new IllegalArgumentException("Usuario no encontrado"));

		Subscription subscription = Subscription.builder()
				.userId(user.getId())
				.externalSourceId(externalSourceId)
				.sourceName(sourceName)
				.build();
		subscriptionRepository.save(subscription);
	}

	public SubscriptionList listSubscriptions(long userId) {

		List<Subscription> subscriptions = subscriptionRepository.findAllByUserId(userId);

		return SubscriptionList.builder()
				.subscriptions(subscriptions.stream().
						map(s -> SubscriptionResponse.builder()
								.externalSourceId(s.getExternalSourceId())
								.sourceName(s.getSourceName())
								.build())
						.toList())
				.build();
	}

}
