package com.subtracker.SubTracker.subscription;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
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
}
