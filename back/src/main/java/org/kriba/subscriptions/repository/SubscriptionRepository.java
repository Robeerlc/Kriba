package org.kriba.subscriptions.repository;

import java.util.List;

import org.kriba.subscriptions.model.Subscription;
import org.springframework.data.jpa.repository.JpaRepository;

public interface SubscriptionRepository extends JpaRepository<Subscription, Long> {
    List<Subscription> findAllByUserId(long userId);
}
