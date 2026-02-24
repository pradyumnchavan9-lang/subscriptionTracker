package com.subtracker.SubTracker.subscription;


import com.subtracker.SubTracker.category.CategoryMapper;
import com.subtracker.SubTracker.user.UserMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class SubscriptionMapper {


    @Autowired
    private UserMapper userMapper;
    @Autowired
    private CategoryMapper categoryMapper;

    //Request to Entity
    public SubscriptionEntity requestToEntity(SubscriptionRequest subscriptionRequest) {
        SubscriptionEntity subscriptionEntity = new SubscriptionEntity();
        subscriptionEntity.setName(subscriptionRequest.getName());
        subscriptionEntity.setCategory(subscriptionRequest.getCategoryEntity());
        subscriptionEntity.setMonthlyPrice(subscriptionRequest.getPrice());
        subscriptionEntity.setStartDate(subscriptionRequest.getStartDate());
        subscriptionEntity.setEndDate(subscriptionRequest.getEndDate());
        return subscriptionEntity;
    }


    //Entity To Response
    public SubscriptionResponse entityToResponse(SubscriptionEntity subscriptionEntity) {
        SubscriptionResponse subscriptionResponse = new SubscriptionResponse();
        subscriptionResponse.setId(subscriptionEntity.getId());
        subscriptionResponse.setName(subscriptionEntity.getName());
        subscriptionResponse.setUserResponseDto(userMapper.entityToResponse(subscriptionEntity.getUser()));
        subscriptionResponse.setCategoryResponse(categoryMapper.entityToResponse(subscriptionEntity.getCategory()));
        subscriptionResponse.setStartDate(subscriptionEntity.getStartDate());
        subscriptionResponse.setEndDate(subscriptionEntity.getEndDate());
        subscriptionResponse.setPrice(subscriptionEntity.getMonthlyPrice());

        return subscriptionResponse;

    }
}
