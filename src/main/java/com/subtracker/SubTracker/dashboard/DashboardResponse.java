package com.subtracker.SubTracker.dashboard;

import com.subtracker.SubTracker.subscription.SubscriptionResponse;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;


@Data
@NoArgsConstructor
@AllArgsConstructor
public class DashboardResponse {

    private Map<Integer,BigDecimal> monthlyExpenses;
    private Integer subscriptions;
    private Integer activeSubscriptions;
    private List<SubscriptionResponse> expiringSubscriptions;

}
