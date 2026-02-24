package com.subtracker.SubTracker.subscription;


import com.subtracker.SubTracker.category.CategoryEntity;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@NoArgsConstructor
public class UpdateSubscriptionRequest {

    private String name;
    private CategoryEntity categoryEntity;
    private BigDecimal price;
}
