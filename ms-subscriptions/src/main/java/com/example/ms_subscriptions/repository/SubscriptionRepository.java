package com.example.ms_subscriptions.repository;

import com.example.ms_subscriptions.model.Subscription;
import com.example.ms_subscriptions.model.SubscriptionType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface SubscriptionRepository extends JpaRepository<Subscription, Long> {

    Optional<Subscription> findByUsername(String username);

    boolean existsByUsernameAndActiveTrue(String username);

    List<Subscription> findByActiveTrue();

    List<Subscription> findByType(SubscriptionType type);

    List<Subscription> findByEndDateBeforeAndActiveTrue(LocalDateTime date);
}
