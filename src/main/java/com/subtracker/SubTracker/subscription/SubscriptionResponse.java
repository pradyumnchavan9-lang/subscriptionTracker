package com.subtracker.SubTracker.subscription;

import com.subtracker.SubTracker.category.CategoryEntity;
import com.subtracker.SubTracker.category.CategoryResponse;
import com.subtracker.SubTracker.user.UserResponseDto;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@NoArgsConstructor
public class SubscriptionResponse {

    private Long id;
    private String name;
    private UserResponseDto userResponseDto;
    private CategoryResponse categoryResponse;
    private BigDecimal price;
    private LocalDateTime startDate;
    private LocalDateTime endDate;
    private LocalDateTime createdDate;
    private LocalDateTime updatedDate;

}
