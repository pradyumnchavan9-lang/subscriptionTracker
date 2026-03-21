package com.subtracker.SubTracker.subscription;

import com.subtracker.SubTracker.category.CategoryEntity;
import com.subtracker.SubTracker.category.CategoryRepository;
import com.subtracker.SubTracker.common.PageMapper;
import com.subtracker.SubTracker.common.PageResponseDto;
import com.subtracker.SubTracker.enums.SubscriptionStatus;
import com.subtracker.SubTracker.idempotency.IdempotencyKeyEntity;
import com.subtracker.SubTracker.idempotency.IdempotencyRepository;
import com.subtracker.SubTracker.payment.PaymentEntity;
import com.subtracker.SubTracker.payment.PaymentRepository;
import com.subtracker.SubTracker.payment.PaymentStatus;
import com.subtracker.SubTracker.user.UserEntity;
import com.subtracker.SubTracker.user.UserRepository;
import jakarta.persistence.OptimisticLockException;
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
import java.util.Optional;

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
    @Autowired
    private IdempotencyRepository idempotencyRepository;
    @Autowired
    private PaymentRepository paymentRepository;


    // Method :----> Create a subscription for a user
    public SubscriptionResponse createSubscription(SubscriptionRequest subscriptionRequest,String idempotencyKey) {

        //Check if idempotency key is present
        if(idempotencyKey == null || idempotencyKey.isBlank()){
            throw new RuntimeException("Idempotency key is required");
        }

        //Fetch User
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        UserEntity userEntity = userRepository.findByEmail(authentication.getName()).
                orElseThrow(NoSuchElementException::new);

        //Fetch Idempotency Key
        Optional<IdempotencyKeyEntity> existing = idempotencyRepository.findByUserIdAndKey(userEntity.getId(),idempotencyKey);
        if(existing.isPresent()) {
           throw new RuntimeException("Duplicate Request Detected");
        }

        //Fetch Subscription and Map to its dto
        SubscriptionEntity subscriptionEntity = subscriptionMapper.requestToEntity(subscriptionRequest);

        subscriptionEntity.setUser(userEntity);

        //Set subscription status as acitve
        subscriptionEntity.setStatus(SubscriptionStatus.ACTIVE);

        //Try saving safely using version
        try {
            subscriptionRepository.save(subscriptionEntity);
        }catch(OptimisticLockException e){
            throw new RuntimeException("Subscription was modified by another user.Please refresh and try again");
        }

        //Save the idempotency key
        IdempotencyKeyEntity keyEntity = new IdempotencyKeyEntity();
        keyEntity.setKey(idempotencyKey);
        keyEntity.setUserId(userEntity.getId());
        idempotencyRepository.save(keyEntity);

        return subscriptionMapper.entityToResponse(subscriptionEntity);
    }

    // Method :----> Get subscription of a user
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


    // Method :----> Get All subscriptions
    public PageResponseDto<SubscriptionResponse> getAllSubscriptions(Pageable pageable) {

        Page<SubscriptionEntity> subscriptionEntities = subscriptionRepository.findAll(pageable);
        Page<SubscriptionResponse> subscriptionResponses = subscriptionEntities.map(subscription ->
                subscriptionMapper.entityToResponse(subscription));

        return pageMapper.pageToPageDto(subscriptionResponses);
    }

    // Method :----> Update Subscription
    public SubscriptionResponse updateSubscription(Long id, UpdateSubscriptionRequest updateSubscriptionRequest) {

        //Fetch subscription from repo
        SubscriptionEntity subscriptionEntity = subscriptionRepository.findById(id)
                .orElseThrow(()->new NoSuchElementException("Subscription with id " + id + " does not exist"));
        subscriptionEntity.onUpdate();

        //Get the update info
        String name = updateSubscriptionRequest.getName();
        BigDecimal price = updateSubscriptionRequest.getPrice();
        CategoryEntity categoryEntity = subscriptionEntity.getCategory();

        //Update Logic
        if(name != null) {
            subscriptionEntity.setName(name);
        }
        if(price != null) {
            subscriptionEntity.setMonthlyPrice(price);
        }
        if(categoryEntity != null) {
            subscriptionEntity.setCategory(categoryEntity);
        }

        //Try saving safely using version
        try {
            subscriptionRepository.save(subscriptionEntity);
        }catch(OptimisticLockException e){
            throw new RuntimeException("Subscription was modified by another user.Please refresh and try again");
        }
        return subscriptionMapper.entityToResponse(subscriptionEntity);
    }

    // Method :----> Delete Subscription
    public boolean findById(Long subscriptionId) {

        //Fetch Subscriptions from repo
        if(subscriptionRepository.findById(subscriptionId).isPresent()) {
            subscriptionRepository.deleteById(subscriptionId);
            return true;
        }else {
            return false;
        }
    }

    // Method :----> Get Price of monthly subscriptions
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

    // Method :----> Renew Subscription
    public PaymentEntity renewSubscription(Long subscriptionId) {

        SubscriptionEntity subscription = subscriptionRepository.findById(subscriptionId)
                .orElseThrow(()->new NoSuchElementException("Subscription with id " + subscriptionId + " does not exist"));

        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        UserEntity user = userRepository.findByEmail(auth.getName())
                .orElseThrow(NoSuchElementException::new);

        if(!user.getId().equals(subscription.getUser().getId())) {
            throw new RuntimeException("You cannot renew this subscription");
        }

        if(subscription.getStatus() == SubscriptionStatus.ACTIVE) {
            throw new RuntimeException("Subscription is already active");
        }

        List<PaymentEntity> payments  = subscription.getPayments();
        for(PaymentEntity payment : payments){
            if(payment.getPaymentStatus() == PaymentStatus.PENDING) {
                return payment;
            }
        }

        PaymentEntity paymentEntity = new PaymentEntity();
        paymentEntity.setPaymentStatus(PaymentStatus.PENDING);
        paymentEntity.setSubscription(subscription);
        paymentEntity.setAmount(subscription.getMonthlyPrice());
        return paymentRepository.save(paymentEntity);
    }
}
