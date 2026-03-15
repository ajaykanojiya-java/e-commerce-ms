package com.example.inventory.client;

import org.springframework.stereotype.Component;

@Component
public class PaymentFallback implements PaymentClient{
    @Override
    public String pay(String productId) {
        return "PaymentFallback: Payment service is currently unavailable. Please try again later.";
    }
}
