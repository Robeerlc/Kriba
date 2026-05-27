package org.kriba.subscriptions.repository;

import org.kriba.subscriptions.model.Subscription;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface SubscriptionRepository extends JpaRepository<Subscription, Long> {
    List<Subscription> findAllByUserId(long userId);

    boolean existsByUserIdAndExternalSourceId(Long userId, String externalSourceId);
}
