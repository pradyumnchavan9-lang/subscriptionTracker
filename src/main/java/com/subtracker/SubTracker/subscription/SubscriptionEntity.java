package com.subtracker.SubTracker.subscription;


import com.subtracker.SubTracker.category.CategoryEntity;
import com.subtracker.SubTracker.enums.SubscriptionStatus;
import com.subtracker.SubTracker.payment.PaymentEntity;
import com.subtracker.SubTracker.user.UserEntity;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Entity
@Data
@NoArgsConstructor
public class SubscriptionEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotNull
    private String name;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id",nullable = false)
    private UserEntity user;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "category_id",nullable = false)
    private CategoryEntity category;

    @OneToMany(mappedBy = "subscription")
    private List<PaymentEntity> payments;

    @Column(nullable = false)
    @NotNull
    private BigDecimal monthlyPrice;

    @Column(nullable = false)
    private LocalDateTime startDate;

    private LocalDateTime endDate;

    @Column(nullable = false)
    private LocalDateTime createdAt;

    private LocalDateTime updatedAt;

    @Version
    private Long version;

    @Enumerated(EnumType.STRING)
    private SubscriptionStatus status;

    @PrePersist
    public void onCreate(){
        createdAt = LocalDateTime.now();
        updatedAt = LocalDateTime.now();
    }

    @PreUpdate
    public void onUpdate(){
        updatedAt = LocalDateTime.now();
    }



}
