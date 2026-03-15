package com.subtracker.SubTracker.ExpiringSoon;


import com.subtracker.SubTracker.enums.SubscriptionStatus;
import com.subtracker.SubTracker.subscription.SubscriptionEntity;
import com.subtracker.SubTracker.subscription.SubscriptionMapper;
import com.subtracker.SubTracker.subscription.SubscriptionRepository;
import com.subtracker.SubTracker.subscription.SubscriptionResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Service
public class ExpiringSoonService {

    @Autowired
    private SubscriptionRepository subscriptionRepository;
    @Autowired
    private SubscriptionMapper subscriptionMapper;

    public List<SubscriptionResponse> findExpiringSoon(){

        //Get current day
        LocalDateTime today =  LocalDateTime.now();
        //Get expiry period
        LocalDateTime nextWeek =  today.plusDays(7);
        //Get all active subscriptions
        List<SubscriptionEntity> subscriptionEntities = subscriptionRepository.findByEndDateBetween(today,nextWeek);
        //List for active subscriptions
        List<SubscriptionResponse> subscriptionResponses = new ArrayList<>();

        for(SubscriptionEntity subscriptionEntity : subscriptionEntities){
            subscriptionResponses.add(subscriptionMapper.entityToResponse(subscriptionEntity));
        }
        return subscriptionResponses;
    }

    public void findExpiredSubscriptions(){
        //Current day
        LocalDateTime today =  LocalDateTime.now();
        //Fetch Subscriptions
        List<SubscriptionEntity> subscriptionEntities = subscriptionRepository.findByEndDateBeforeAndStatus(today,SubscriptionStatus.ACTIVE);
        for(SubscriptionEntity subscription : subscriptionEntities){
            subscription.setStatus(SubscriptionStatus.EXPIRED);
        }
        subscriptionRepository.saveAll(subscriptionEntities);
    }
}
