
package com.example.order.controller;
import com.example.order.client.InventoryClient;
import com.example.order.util.TracingUtil;
import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/order")
public class OrderController {

 private final InventoryClient inventoryClient;
 private final TracingUtil tracingUtil;

 public OrderController(InventoryClient inventoryClient, TracingUtil tracingUtil){
  this.inventoryClient = inventoryClient;
  this.tracingUtil = tracingUtil;
 }

 @GetMapping("/{productId}")
 @CircuitBreaker(name="inventoryService", fallbackMethod="fallback")
 public String placeOrder(@PathVariable String productId){
   tracingUtil.logInfo("ORDER-SERVICE", "placeOrder", "Processing order for productId: " + productId);
   
   int stock = inventoryClient.getStock(productId);
   tracingUtil.logDebug("ORDER-SERVICE", "placeOrder", "Received stock: " + stock + " from Inventory Service");
   
   if(stock>0) {
     tracingUtil.logInfo("ORDER-SERVICE", "placeOrder", "Order placed successfully for productId: " + productId);
     return "Order placed";
   }
   tracingUtil.logInfo("ORDER-SERVICE", "placeOrder", "Out of stock for productId: " + productId);
   return "Out of stock";
 }

 public String fallback(String productId, Throwable t){
     tracingUtil.logError("ORDER-SERVICE", "placeOrder", "Circuit breaker triggered for productId: " + productId, (Exception) t);
     return "Fallback: Inventory service down. Try later.";
 }
}
