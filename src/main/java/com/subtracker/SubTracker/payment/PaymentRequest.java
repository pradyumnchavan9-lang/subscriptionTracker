package com.subtracker.SubTracker.payment;


import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class PaymentRequest {

    private Long paymentId;
    private PaymentStatus paymentStatus;
    private String paymentSignature = "123";
}
