package org.kriba.subscriptions.repository;

import org.kriba.subscriptions.model.Subscription;
import org.springframework.data.jpa.repository.JpaRepository;

public interface SubscriptionRepository extends JpaRepository<Subscription, Long> {
}
