package com.subtracker.SubTracker.ExpiringSoon;


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

        LocalDateTime today =  LocalDateTime.now();
        LocalDateTime nextWeek =  today.plusDays(7);
        System.out.println(today);
        System.out.println(nextWeek);
        List<SubscriptionEntity> subscriptionEntities = subscriptionRepository.findByEndDateBetween(today,nextWeek);
        List<SubscriptionResponse> subscriptionResponses = new ArrayList<>();
        for(SubscriptionEntity subscriptionEntity : subscriptionEntities){
            subscriptionResponses.add(subscriptionMapper.entityToResponse(subscriptionEntity));
        }
        return subscriptionResponses;
    }
}
