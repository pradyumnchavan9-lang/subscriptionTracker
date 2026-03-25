package com.subtracker.SubTracker.subscription;

import com.subtracker.SubTracker.category.CategoryEntity;
import com.subtracker.SubTracker.category.CategoryRepository;
import com.subtracker.SubTracker.common.PageMapper;
import com.subtracker.SubTracker.common.PageResponseDto;
import com.subtracker.SubTracker.enums.SubscriptionStatus;
import com.subtracker.SubTracker.idempotency.IdempotencyKeyEntity;
import com.subtracker.SubTracker.idempotency.IdempotencyRepository;
import com.subtracker.SubTracker.payment.*;
import com.subtracker.SubTracker.user.UserEntity;
import com.subtracker.SubTracker.user.UserRepository;
import jakarta.persistence.OptimisticLockException;
import jakarta.transaction.Transactional;
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
    @Autowired
    private PaymentMapper paymentMapper;


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
    @Transactional
    public PaymentResponse renewSubscription(Long subscriptionId) {

        SubscriptionEntity subscription = subscriptionRepository.findById(subscriptionId)
                .orElseThrow(()->new NoSuchElementException("Subscription with id " + subscriptionId + " does not exist"));

        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        UserEntity user = userRepository.findByEmail(auth.getName())
                .orElseThrow(NoSuchElementException::new);

        if(!user.getId().equals(subscription.getUser().getId())) {
            throw new RuntimeException("You cannot renew this subscription");
        }

        if(subscription.getStatus() == SubscriptionStatus.ACTIVE){
            throw new RuntimeException("Subscription is already active");
        }

        Optional<PaymentEntity> payment  = paymentRepository.findBySubscriptionAndPaymentStatus(subscription, PaymentStatus.PENDING);

            if(payment.isPresent()) {
                String paymentUrl = "http://localhost:8080/payments/" + payment.get().getPaymentId() + "/pay";
                PaymentResponse paymentResponse = paymentMapper.entityToResponse(payment.get());
                paymentResponse.setPaymentUrl(paymentUrl);
                return paymentResponse;
            }

        PaymentEntity paymentEntity = new PaymentEntity();
        paymentEntity.setPaymentStatus(PaymentStatus.PENDING);
        paymentEntity.setSubscription(subscription);
        paymentEntity.setAmount(subscription.getMonthlyPrice());
        subscription.setStatus(SubscriptionStatus.PAYMENT_PENDING);
        subscriptionRepository.save(subscription);
        paymentRepository.save(paymentEntity);

        String paymentUrl = "http://localhost:8080/payments/" + paymentEntity.getPaymentId() +"/pay";

        PaymentResponse paymentResponse = new PaymentResponse();
        paymentResponse.setPaymentId(paymentEntity.getPaymentId());
        paymentResponse.setPaymentStatus(paymentEntity.getPaymentStatus());
        paymentResponse.setAmount(paymentEntity.getAmount());
        paymentResponse.setPaymentUrl(paymentUrl);

        return paymentResponse;
    }

    //Method :-----> Complete Payment
    @Transactional
    public void completePayment(Long paymentId){

        //Fetch Payment
        PaymentEntity payment = paymentRepository.findByPaymentId(paymentId)
                .orElseThrow(()->new NoSuchElementException("Payment with id " + paymentId + " does not exist"));
        //Check Status
        if(!payment.getPaymentStatus().equals(PaymentStatus.PENDING)) {
            throw new RuntimeException("Payment status is " + payment.getPaymentStatus()) ;
        }
        payment.setPaymentStatus(PaymentStatus.SUCCESS);


        //Update End Date
        LocalDateTime now  = LocalDateTime.now();
        LocalDateTime newEndDate;
        SubscriptionEntity subscription = payment.getSubscription();
        if(subscription.getEndDate().isAfter(now)) {
            newEndDate = subscription.getEndDate().plusMonths(1);
        }else{
            newEndDate = now.plusMonths(1);
        }
        subscription.setEndDate(newEndDate);

        //Save changes
        subscription.setStatus(SubscriptionStatus.ACTIVE);
        paymentRepository.save(payment);
        subscriptionRepository.save(subscription);

    }

    //Method :----> webhook for payment
    @Transactional
    public void subscriptionAfterPayment(PaymentRequest paymentRequest){

        // 1 Verify signature
        if(!paymentRequest.getPaymentSignature().equals("123")){
            return;
        }

        // 2 Fetch Payment
        PaymentEntity payment = paymentRepository.findByPaymentId(paymentRequest.getPaymentId())
                .orElseThrow(()->new NoSuchElementException("Payment with id " + paymentRequest.getPaymentId() + " does not exist"));

        // 3 Idempotency check
        if(!payment.getPaymentStatus().equals(PaymentStatus.PENDING)) {
            return;
        }

        // 4 Update Based on Status
        if(paymentRequest.getPaymentStatus().equals(PaymentStatus.SUCCESS)){
            //call complete payment
            completePayment(paymentRequest.getPaymentId());
        }
        else{
            payment.setPaymentStatus(PaymentStatus.FAILED);
            paymentRepository.save(payment);
        }

    }
}
