package com.subtracker.SubTracker.subscription;

import com.subtracker.SubTracker.category.CategoryEntity;
import com.subtracker.SubTracker.common.PageMapper;
import com.subtracker.SubTracker.common.PageResponseDto;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.NoSuchElementException;

@Service
public class SubscriptionService {

    @Autowired
    private SubscriptionRepository subscriptionRepository;
    @Autowired
    private SubscriptionMapper subscriptionMapper;
    @Autowired
    private PageMapper pageMapper;


    //Create a subscription for a user
    public SubscriptionResponse createSubscription(SubscriptionRequest subscriptionRequest) {
        SubscriptionEntity subscriptionEntity = subscriptionMapper.requestToEntity(subscriptionRequest);
        subscriptionEntity.onCreate();
        //Get User from Security Context Holder and set here -> subscription.setUser(user)
        subscriptionRepository.save(subscriptionEntity);

        return subscriptionMapper.entityToResponse(subscriptionEntity);
    }

    //Get subscription of a user
    public SubscriptionResponse getSubscriptionById(Long subscriptionId) {
        SubscriptionEntity subscriptionEntity = subscriptionRepository.findById(subscriptionId)
                .orElseThrow(()->new NoSuchElementException("Subscription with id " + subscriptionId + " does not exist"));
        return subscriptionMapper.entityToResponse(subscriptionEntity);
    }

    public PageResponseDto<SubscriptionResponse> findAllByUserId(Long userId, Pageable pageable) {

        Page<SubscriptionEntity> subscriptionEntities = subscriptionRepository.findAllByUserId(userId,pageable);
        Page<SubscriptionResponse> subscriptionResponses = subscriptionEntities.map(subscription ->
                subscriptionMapper.entityToResponse(subscription));
        return pageMapper.pageToPageDto(subscriptionResponses);
    }


    //Get All subscriptions
    public PageResponseDto<SubscriptionResponse> getAllSubscriptions(Pageable pageable) {

        Page<SubscriptionEntity> subscriptionEntities = subscriptionRepository.findAll(pageable);
        Page<SubscriptionResponse> subscriptionResponses = subscriptionEntities.map(subscription ->
                subscriptionMapper.entityToResponse(subscription));

        return pageMapper.pageToPageDto(subscriptionResponses);
    }

    //Update Subscription
    public SubscriptionResponse updateSubscription(Long id, UpdateSubscriptionRequest updateSubscriptionRequest) {

        SubscriptionEntity subscriptionEntity = subscriptionRepository.findById(id)
                .orElseThrow(()->new NoSuchElementException("Subscription with id " + id + " does not exist"));
        subscriptionEntity.onUpdate();
        String name = updateSubscriptionRequest.getName();
        BigDecimal price = updateSubscriptionRequest.getPrice();
        CategoryEntity categoryEntity = subscriptionEntity.getCategory();
        if(name != null) {
            subscriptionEntity.setName(name);
        }
        if(price != null) {
            subscriptionEntity.setMonthlyPrice(price);
        }
        if(categoryEntity != null) {
            subscriptionEntity.setCategory(categoryEntity);
        }
        subscriptionRepository.save(subscriptionEntity);
        return subscriptionMapper.entityToResponse(subscriptionEntity);
    }

    //Delete Subscription
    public boolean findById(Long subscriptionId) {

        if(subscriptionRepository.findById(subscriptionId).isPresent()) {
            subscriptionRepository.deleteById(subscriptionId);
            return true;
        }else {
            return false;
        }
    }
}
