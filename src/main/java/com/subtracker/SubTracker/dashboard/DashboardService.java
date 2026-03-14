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
import java.util.*;

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
        Map<Integer, BigDecimal> monthlyExpenses = new LinkedHashMap<>();
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

    //Categorical Expenses
    public Map<String,BigDecimal> getCategoricalExpenses(List<SubscriptionEntity> subscriptions) {

        LocalDate today =  LocalDate.now();
        HashMap<String,BigDecimal> categoricalExpenses = new HashMap<>();
        for(SubscriptionEntity subscription : subscriptions) {
            LocalDate start = subscription.getStartDate().toLocalDate();
            LocalDate end = subscription.getEndDate().toLocalDate();

            if(!today.isBefore(start) && !today.isAfter(end)) {
                String category = subscription.getCategory().getName();
                BigDecimal previous = categoricalExpenses.getOrDefault(category, BigDecimal.ZERO);
                categoricalExpenses.put(
                        category,
                        previous.add(subscription.getMonthlyPrice())
                );
            }
        }
        return categoricalExpenses;
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

        //Category Expenses
        Map<String,BigDecimal> categoryExpenses = getCategoricalExpenses(allSubscriptions);
        dashboardResponse.setCategoryExpenses(categoryExpenses);

        //Current Month
        int month = today.getMonthValue();
        dashboardResponse.setCurrentMonth(month);
        return dashboardResponse;
    }


    //Get Top subcriptions
    public TopSubscriptionsResponse getTopSubscriptions(){

        LocalDate today = LocalDate.now();
        Map<String,BigDecimal> topSubscriptions = new LinkedHashMap<>();
        Authentication auth =  SecurityContextHolder.getContext().getAuthentication();
        UserEntity user = userRepository.findByEmail(auth.getName()).orElseThrow(NoSuchElementException:: new);
        List<SubscriptionEntity> subscriptions = subscriptionRepository.findAllByUserId(user.getId());
        List<SubscriptionEntity> activeSubscriptions = new ArrayList<>();
        for(SubscriptionEntity subscription : subscriptions){

            LocalDate start = subscription.getStartDate().toLocalDate();
            LocalDate end = subscription.getEndDate().toLocalDate();
            if(!today.isBefore(start) && !today.isAfter(end)) {
                activeSubscriptions.add(subscription);
            }
        }

        activeSubscriptions.sort((a,b) -> b.getMonthlyPrice().compareTo(a.getMonthlyPrice()));
        for(int i = 0; i < Math.min(3,activeSubscriptions.size()); i++){
            topSubscriptions.put(activeSubscriptions.get(i).getName(),activeSubscriptions.get(i).getMonthlyPrice());
        }

        TopSubscriptionsResponse topSubscriptionsResponse = new TopSubscriptionsResponse();
        topSubscriptionsResponse.setTopSubscriptions(topSubscriptions);
        return topSubscriptionsResponse;
    }
}
