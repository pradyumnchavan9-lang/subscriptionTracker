package com.subtracker.SubTracker.subscription;

import com.subtracker.SubTracker.enums.SubscriptionStatus;
import com.subtracker.SubTracker.payment.PaymentEntity;
import com.subtracker.SubTracker.payment.PaymentStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Date;
import java.util.List;
import java.util.Optional;

@Repository
public interface SubscriptionRepository extends JpaRepository<SubscriptionEntity,Long> {

    Page<SubscriptionEntity> findAllByUserId(Long userId, Pageable pageable);
    Page<SubscriptionEntity> findAll(Pageable pageable);
    List<SubscriptionEntity> findByEndDateBetween(LocalDateTime start, LocalDateTime end);
    List<SubscriptionEntity> findAllByUserId(Long userId);

    @Query("""
        SELECT s FROM SubscriptionEntity s
        WHERE s.user.id = ?1
        AND s.startDate <= ?3
        AND s.endDate >= ?2
        """)


    List<SubscriptionEntity> findMonthlySubscriptions(Long userId,LocalDateTime startDate,LocalDateTime endDate);

    //Marking expired subscriptions
    List<SubscriptionEntity> findByEndDateBeforeAndStatus(LocalDateTime today, SubscriptionStatus status);

}
