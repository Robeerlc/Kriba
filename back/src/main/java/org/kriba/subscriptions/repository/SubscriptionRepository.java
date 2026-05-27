package org.kriba.subscriptions.repository;

import org.kriba.subscriptions.model.Subscription;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface SubscriptionRepository extends JpaRepository<Subscription, Long> {
    List<Subscription> findAllByUserId(long userId);
    Optional<Subscription> findByUserIdAndExternalSourceIdAndSourceName(
            long userId,
            String externalSourceId,
            String sourceName
    );
}