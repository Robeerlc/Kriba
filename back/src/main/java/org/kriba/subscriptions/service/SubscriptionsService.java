package org.kriba.subscriptions.service;

import org.kriba.subscriptions.dto.SubscriptionList;
import org.kriba.subscriptions.dto.SubscriptionResponse;
import org.kriba.subscriptions.model.Subscription;
import org.kriba.subscriptions.repository.SubscriptionRepository;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.NoSuchElementException;

@Service
public class SubscriptionsService {

    private final SubscriptionRepository subscriptionRepository;

    public SubscriptionsService(SubscriptionRepository subscriptionRepository) {
        this.subscriptionRepository = subscriptionRepository;
    }

    public void subscribe(long userId, String externalSourceId, String sourceName) {

        if (subscriptionRepository
                .findByUserIdAndExternalSourceIdAndSourceName(userId, externalSourceId, sourceName)
                .isPresent()) {
            throw new IllegalArgumentException("Ya estás suscrito a esta fuente");
        }
        Subscription subscription = Subscription.builder()
                .userId(userId)
                .externalSourceId(externalSourceId)
                .sourceName(sourceName).build();
        subscriptionRepository.save(subscription);
    }

    public void unsubscribe(long userId, String externalSourceId, String sourceName) {
        Subscription subscription = subscriptionRepository
                .findByUserIdAndExternalSourceIdAndSourceName(userId, externalSourceId, sourceName)
                .orElseThrow(() -> new NoSuchElementException("Suscripción no encontrada"));
        subscriptionRepository.delete(subscription);
    }

    public SubscriptionList getSubscriptions(long userId) {
        List<Subscription> subscriptions = subscriptionRepository.findAllByUserId(userId);
        return SubscriptionList.builder()
                .subscriptions(subscriptions.stream()
                        .map(s -> SubscriptionResponse.builder()
                                .externalSourceId(s.getExternalSourceId())
                                .sourceName(s.getSourceName())
                                .build())
                        .toList())
                .build();
    }
}