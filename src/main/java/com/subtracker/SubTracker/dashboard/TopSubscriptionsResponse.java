package com.subtracker.SubTracker.dashboard;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.Map;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class TopSubscriptionsResponse {

    Map<String, BigDecimal> topSubscriptions;
}
