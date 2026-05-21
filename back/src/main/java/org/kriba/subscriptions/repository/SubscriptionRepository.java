package org.kriba.subscriptions.repository;

import org.kriba.subscriptions.model.Subscription;
import org.kriba.users.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;

public interface SubscriptionRepository extends JpaRepository<Subscription, Long> {
    Optional<User> findByEmail(String email);
}
