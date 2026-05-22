package org.kriba.subscriptions.service;

import org.kriba.subscriptions.dto.SubscriptionList;
import org.kriba.subscriptions.dto.SubscriptionResponse;
import org.kriba.subscriptions.model.Subscription;
import org.kriba.subscriptions.repository.SubscriptionRepository;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class SubscriptionsService {

    private final SubscriptionRepository subscriptionRepository;

    public SubscriptionsService(SubscriptionRepository subscriptionRepository) {
        this.subscriptionRepository = subscriptionRepository;
    }

    public void subscribe(long userId, String externalSourceId, String sourceName) {


        Subscription subscription = Subscription.builder()
                .userId(userId)
                .externalSourceId(externalSourceId)
                .sourceName(sourceName)
                .build();
        subscriptionRepository.save(subscription);
    }

    public SubscriptionList getSubscriptions(long userId) {

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
