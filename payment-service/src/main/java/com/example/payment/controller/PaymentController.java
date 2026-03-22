
package com.example.payment.controller;
import com.example.payment.util.TracingUtil;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/payment")
public class PaymentController {
    private final TracingUtil tracingUtil;
    
    public PaymentController(TracingUtil tracingUtil) {
        this.tracingUtil = tracingUtil;
    }

 @PostMapping("/pay/{productID}")
 public String pay(@PathVariable("productID") String productID){
     tracingUtil.logInfo("PAYMENT-SERVICE", "pay", "Processing payment for productID: " + productID);
     
     if(productID.equals("1")){ // Simulate payment failure for productID 1
         tracingUtil.logError("PAYMENT-SERVICE", "pay", "Payment failed for product ID: " + productID, 
             new RuntimeException("Simulated payment failure"));
         throw new RuntimeException("Payment failed for product ID: " + productID);
     }
     tracingUtil.logInfo("PAYMENT-SERVICE", "pay", "Payment successful for product ID: " + productID);
     return "Payment Success for product ID: " + productID;
 }
}
