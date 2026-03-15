package com.subtracker.SubTracker.ExpiringSoon;

import com.subtracker.SubTracker.category.CategoryResponse;
import com.subtracker.SubTracker.notification.EmailService;
import com.subtracker.SubTracker.subscription.SubscriptionResponse;
import com.subtracker.SubTracker.user.UserResponseDto;
import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class ExpiryReminderService {

    @Autowired
    private ExpiringSoonService expiringSoonService;

    @Autowired
    private EmailService emailService;

    @Scheduled(cron = "0 0 9 * * *")
    @Transactional
    public void checkExpirations(){

        List<SubscriptionResponse> subscriptionResponses = expiringSoonService.findExpiringSoon();
        for( SubscriptionResponse subscriptionResponse : subscriptionResponses){
            UserResponseDto user = subscriptionResponse.getUserResponseDto();
            CategoryResponse category = subscriptionResponse.getCategoryResponse();
            String content =
                    "Dear " + user.getName() +
                            ", your subscription for " + category.getName() +
                            " ends on " + subscriptionResponse.getEndDate() + ".";
            emailService.sendEmail(user.getEmail(),"Subscription Expiring Soon",content);
        }
    }

    @Scheduled(cron = "0 0 2 * * *")
    @Transactional
    public void findExpiredSubscriptions(){
        expiringSoonService.findExpiredSubscriptions();
    }

}
