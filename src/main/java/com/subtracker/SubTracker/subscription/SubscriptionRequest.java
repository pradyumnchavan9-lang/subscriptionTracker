package com.subtracker.SubTracker.subscription;


import com.subtracker.SubTracker.category.CategoryEntity;
import com.subtracker.SubTracker.category.CategoryRequest;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@NoArgsConstructor
public class SubscriptionRequest {

    private String name;
    private CategoryEntity categoryEntity;
    private BigDecimal price;
    private LocalDateTime startDate;
    private LocalDateTime endDate;

}
