package com.subtracker.SubTracker.payment;

import com.subtracker.SubTracker.subscription.SubscriptionEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface PaymentRepository extends JpaRepository<PaymentEntity, Long> {
    
    Optional<PaymentEntity> findBySubscriptionAndPaymentStatus(SubscriptionEntity subscription, PaymentStatus paymentStatus);

    Optional<PaymentEntity> findByPaymentId(Long paymentId);

    Optional<PaymentEntity> findByStripeSessionId(String sessionId);
}
