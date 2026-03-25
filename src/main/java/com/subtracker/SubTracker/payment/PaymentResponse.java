package com.subtracker.SubTracker.payment;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class PaymentResponse {

    private Long paymentId;
    private String paymentUrl;
    private BigDecimal amount;
    private PaymentStatus paymentStatus;

}
