package com.example.inventory.controller;
import com.example.inventory.client.PaymentClient;
import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/inventory")
public class InventoryController {
    private final PaymentClient paymentClient;

    public InventoryController(PaymentClient paymentClient) {
        this.paymentClient = paymentClient;
    }
    @GetMapping("/{productId}")
    @CircuitBreaker(name="paymentService", fallbackMethod="fallback")
    public int getStock(@PathVariable String productId){
        String result = paymentClient.pay(productId); // call payment service
        if(result.contains("Payment Success")){
            return 10; // Simulate stock available
        }
        return 0; // Simulate out of stock if payment fails
    }

    // Resilience4j looks for a fallback method with same arguments as the original
    // method plus an Exception/Throwable. Using Exception here improves compatibility
    // with the fallback resolution in some proxying scenarios.
    public int fallback(String productId, Exception ex){
        return -1; // Indicate failure to get stock due to payment service down
    }
}
