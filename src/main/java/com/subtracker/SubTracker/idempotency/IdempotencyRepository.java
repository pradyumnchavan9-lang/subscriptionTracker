package com.subtracker.SubTracker.idempotency;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface IdempotencyRepository extends JpaRepository<IdempotencyKeyEntity, Long> {

    Optional<IdempotencyKeyEntity> findByUserIdAndKey(Long userId, String key);
}
