package com.subtracker.SubTracker.subscription;

import com.stripe.exception.StripeException;
import com.stripe.model.Event;
import com.stripe.model.checkout.Session;
import com.stripe.net.Webhook;
import com.stripe.param.checkout.SessionCreateParams;
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
            return ;
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
    public void handleStripeWebhook(String payload, String sigHeader) {

        String endpointSecret = "abc";

        Event event;

        // 1. Verify Stripe signature
        try {
            event = Webhook.constructEvent(payload, sigHeader, endpointSecret);
        } catch (Exception e) {
            throw new RuntimeException("Invalid Stripe signature");
        }

        // 2. Handle only relevant event
        if ("checkout.session.completed".equals(event.getType())) {

            // 3. Extract session object
            Session session = (Session) event.getDataObjectDeserializer()
                    .getObject()
                    .orElseThrow(() -> new RuntimeException("Failed to deserialize session"));

            // 4. Get sessionId
            String sessionId = session.getId();

            // 5. Find payment using sessionId
            PaymentEntity payment = paymentRepository
                    .findByStripeSessionId(sessionId)
                    .orElseThrow(() -> new RuntimeException("Payment not found"));

            // 6. Idempotency check
            if (payment.getPaymentStatus() != PaymentStatus.PENDING) {
                return;
            }

            // 7. Update status
            completePayment(payment.getPaymentId());
            paymentRepository.save(payment);
        }

        // Optional: handle failed/expired cases
        else if ("checkout.session.expired".equals(event.getType())) {

            Session session = (Session) event.getDataObjectDeserializer()
                    .getObject()
                    .orElseThrow(() -> new RuntimeException("Failed to deserialize session"));

            String sessionId = session.getId();

            PaymentEntity payment = paymentRepository
                    .findByStripeSessionId(sessionId)
                    .orElseThrow(() -> new RuntimeException("Payment not found"));

            if (payment.getPaymentStatus() != PaymentStatus.PENDING) {
                return;
            }

            payment.setPaymentStatus(PaymentStatus.FAILED);
            paymentRepository.save(payment);
        }
    }

    //Method :----> stripe checkout session
    public CheckoutResponse createStripeCheckoutSession(Long paymentId) {

        PaymentEntity payment = paymentRepository.findByPaymentId(paymentId)
                .orElseThrow(()->new NoSuchElementException("Payment with id " + paymentId + " does not exist"));

        //1. Convert amount to the smallest currency unit
        long amountInCents = payment.getAmount().multiply(new BigDecimal(100)).longValue();

        //2. Build checkout session params
        SessionCreateParams params = SessionCreateParams.builder()
                .setMode(SessionCreateParams.Mode.PAYMENT)
                .setSuccessUrl("http://localhost:8080/payments/success?session_id={CHECKOUT_SESSION_ID}")
                .setCancelUrl("http://localhost:8080/payments/cancel")
                .putMetadata("paymentId", paymentId.toString())
                .addLineItem(
                        SessionCreateParams.LineItem.builder()
                                .setQuantity(1L)
                                .setPriceData(
                                        SessionCreateParams.LineItem.PriceData.builder()
                                                .setCurrency("INR")
                                                .setUnitAmount(amountInCents)
                                                .setProductData(
                                                        SessionCreateParams.LineItem.PriceData.ProductData.builder()
                                                                .setName("Payment #" + paymentId)
                                                                .build()
                                                )
                                                .build()
                                )
                                .build()
                )
                .build();


        if(payment.getPaymentStatus() != PaymentStatus.PENDING) {
            throw new RuntimeException("Payment already processed or not eligible for checkout");
        }

        //3.Create Session
        Session session = null;
        try {
            session = Session.create(params);
        } catch (StripeException e) {
            throw new RuntimeException(e);
        }
        payment.setStripeSessionId(session.getId());
        paymentRepository.save(payment);

        //4.Return url to frontend
        CheckoutResponse checkoutResponse = new CheckoutResponse();
        checkoutResponse.setUrl(session.getUrl());
        return checkoutResponse;
    }
}
