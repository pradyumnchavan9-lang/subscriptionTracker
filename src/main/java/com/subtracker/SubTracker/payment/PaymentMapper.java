package com.subtracker.SubTracker.payment;

import org.springframework.stereotype.Component;

@Component
public class PaymentMapper {

    public PaymentResponse entityToResponse(PaymentEntity paymentEntity) {
        PaymentResponse paymentResponse = new PaymentResponse();
        paymentResponse.setPaymentStatus(paymentEntity.getPaymentStatus());
        paymentResponse.setAmount(paymentEntity.getAmount());
        paymentResponse.setPaymentId(paymentEntity.getPaymentId());
        return paymentResponse;
    }
}
