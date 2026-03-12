package com.subtracker.SubTracker.subscription;

import com.subtracker.SubTracker.category.CategoryEntity;
import com.subtracker.SubTracker.category.CategoryRepository;
import com.subtracker.SubTracker.common.PageMapper;
import com.subtracker.SubTracker.common.PageResponseDto;
import com.subtracker.SubTracker.user.UserEntity;
import com.subtracker.SubTracker.user.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.YearMonth;
import java.util.List;
import java.util.NoSuchElementException;

@Service
public class SubscriptionService {

    @Autowired
    private SubscriptionRepository subscriptionRepository;
    @Autowired
    private SubscriptionMapper subscriptionMapper;
    @Autowired
    private PageMapper pageMapper;
    @Autowired
    private UserRepository userRepository;
    @Autowired
    private CategoryRepository categoryRepository;


    //Create a subscription for a user
    public SubscriptionResponse createSubscription(SubscriptionRequest subscriptionRequest) {
        SubscriptionEntity subscriptionEntity = subscriptionMapper.requestToEntity(subscriptionRequest);
        subscriptionEntity.onCreate();
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        UserEntity userEntity = userRepository.findByEmail(authentication.getName()).
                orElseThrow(NoSuchElementException::new);
        subscriptionEntity.setUser(userEntity);
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

    //Get Price of monthly subscriptions
    public BigDecimal getMonthlyPrice(int year,int month) {

        LocalDate start = YearMonth.now().atDay(1);
        LocalDate end = YearMonth.now().atEndOfMonth();

        LocalDateTime monthStart = start.atStartOfDay();
        LocalDateTime monthEnd = end.atTime(23,59,59);
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        UserEntity user = userRepository.findByEmail(authentication.getName())
                .orElseThrow(NoSuchElementException::new);
        List<SubscriptionEntity> subscriptionEntities = subscriptionRepository.findMonthlySubscriptions(user.getId(),monthStart,monthEnd);
        BigDecimal monthlyPrice = BigDecimal.ZERO;
        for(SubscriptionEntity subscriptionEntity : subscriptionEntities) {
            monthlyPrice = monthlyPrice.add(subscriptionEntity.getMonthlyPrice());
        }
        return monthlyPrice;
    }
}
