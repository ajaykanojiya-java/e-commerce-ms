package com.example.inventory.controller;
import com.example.inventory.client.PaymentClient;
import com.example.inventory.util.TracingUtil;
import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/inventory")
public class InventoryController {
    private final PaymentClient paymentClient;
    private final TracingUtil tracingUtil;

    public InventoryController(PaymentClient paymentClient, TracingUtil tracingUtil) {
        this.paymentClient = paymentClient;
        this.tracingUtil = tracingUtil;
    }
    
    @GetMapping("/{productId}")
    @CircuitBreaker(name="paymentService", fallbackMethod="fallback")
    public int getStock(@PathVariable String productId){
        tracingUtil.logInfo("INVENTORY-SERVICE", "getStock", "Fetching stock for productId: " + productId);
        
        String result = paymentClient.pay(productId); // call payment service
        tracingUtil.logDebug("INVENTORY-SERVICE", "getStock", "Payment result: " + result);
        
        if(result.contains("Payment Success")){
            tracingUtil.logInfo("INVENTORY-SERVICE", "getStock", "Payment successful, returning stock 10 for productId: " + productId);
            return 10; // Simulate stock available
        }
        tracingUtil.logInfo("INVENTORY-SERVICE", "getStock", "Payment failed, stock unavailable for productId: " + productId);
        return 0; // Simulate out of stock if payment fails
    }

    // Resilience4j looks for a fallback method with same arguments as the original
    // method plus an Exception/Throwable. Using Exception here improves compatibility
    // with the fallback resolution in some proxying scenarios.
    public int fallback(String productId, Exception ex){
        tracingUtil.logError("INVENTORY-SERVICE", "getStock", "Circuit breaker triggered for productId: " + productId, ex);
        return -1; // Indicate failure to get stock due to payment service down
    }
}
