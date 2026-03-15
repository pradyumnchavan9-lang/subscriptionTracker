package com.subtracker.SubTracker.idempotency;



import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Table(
        uniqueConstraints = @UniqueConstraint(
                columnNames = {"userId", "key"}
        )
)
@Data
@NoArgsConstructor
public class IdempotencyKeyEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String key;

    @NotNull
    private Long userId;

    private LocalDateTime createdAt;

    @PrePersist
    public void onCreate(){
        this.createdAt = LocalDateTime.now();
    }
}