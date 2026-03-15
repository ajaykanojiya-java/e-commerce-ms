
package com.example.payment.controller;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/payment")
public class PaymentController {

 @PostMapping("/pay/{productID}")
 public String pay(@PathVariable("productID") String productID){
     if(productID.equals("1")){ // Simulate payment failure for productID 1
         throw new RuntimeException("Payment failed for product ID: " + productID);
     }
     return "Payment Success for product ID: " + productID;
 }
}
