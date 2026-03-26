package com.subtracker.SubTracker.config;

import com.stripe.Stripe;
import jakarta.annotation.PostConstruct;
import org.springframework.stereotype.Service;


@Service
public class StripeConfig {

    @PostConstruct
    public void init() {
        // Set your test secret key here
        Stripe.apiKey = "abc";
    }
}