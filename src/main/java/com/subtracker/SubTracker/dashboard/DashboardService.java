package com.subtracker.SubTracker.dashboard;

import com.subtracker.SubTracker.ExpiringSoon.ExpiringSoonService;
import com.subtracker.SubTracker.subscription.SubscriptionEntity;
import com.subtracker.SubTracker.subscription.SubscriptionRepository;
import com.subtracker.SubTracker.user.UserEntity;
import com.subtracker.SubTracker.user.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;

@Service
public class DashboardService {

    @Autowired
    private ExpiringSoonService expiringSoonService;
    @Autowired
    private SubscriptionRepository subscriptionRepository;
    @Autowired
    private UserRepository userRepository;

    public Map<Integer, BigDecimal> getMonthlyExpenses(List<SubscriptionEntity> subscriptions) {

        LocalDateTime now = LocalDateTime.now();
        Map<Integer, BigDecimal> monthlyExpenses = new HashMap<>();
        int year = now.getYear();

        for (int i = 1; i <= 12; i++) {

            LocalDate monthStart = LocalDate.of(year, i, 1);
            LocalDate monthEnd = monthStart.withDayOfMonth(monthStart.lengthOfMonth());

            BigDecimal monthlyExpense = BigDecimal.ZERO;

            for (SubscriptionEntity subscription : subscriptions) {

                LocalDate start = subscription.getStartDate().toLocalDate();
                LocalDate end = subscription.getEndDate().toLocalDate();

                if (!start.isAfter(monthEnd) && !end.isBefore(monthStart)) {
                    monthlyExpense = monthlyExpense.add(subscription.getMonthlyPrice());
                }
            }

            monthlyExpenses.put(i, monthlyExpense);
        }

        return monthlyExpenses;
    }

    public DashboardResponse getDashboard(){
        DashboardResponse dashboardResponse = new DashboardResponse();

        //Upcoming Payments
        dashboardResponse.setExpiringSubscriptions(expiringSoonService.findExpiringSoon());

        //Subscriptions Count
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        UserEntity user = userRepository.findByEmail(authentication.getName())
                .orElseThrow(NoSuchElementException:: new);
        List<SubscriptionEntity> allSubscriptions = subscriptionRepository.findAllByUserId(user.getId());
        dashboardResponse.setSubscriptions(allSubscriptions.size());
        //Current Subscriptions Count
        LocalDateTime today = LocalDateTime.now();
        int count = 0;
        for(SubscriptionEntity subscription : allSubscriptions){
            if(today.isAfter(subscription.getStartDate()) && today.isBefore(subscription.getEndDate())){
                count++;
            }
        }
        dashboardResponse.setActiveSubscriptions(count);

        //Monthly Expenses
        Map<Integer,BigDecimal> monthlyExpenses = getMonthlyExpenses(allSubscriptions);
        dashboardResponse.setMonthlyExpenses(monthlyExpenses);
        return dashboardResponse;
    }
}
