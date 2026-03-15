package com.example.inventory.client;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;

@FeignClient(name="payment-service", fallback=PaymentFallback.class)
public interface PaymentClient {
    @PostMapping ("/payment/pay/{productId}")
    String pay(@PathVariable("productId") String productId);
}
