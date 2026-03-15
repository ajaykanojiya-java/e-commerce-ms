
package com.example.order.controller;
import com.example.order.client.InventoryClient;
import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/order")
public class OrderController {

 private final InventoryClient inventoryClient;

 public OrderController(InventoryClient inventoryClient){
  this.inventoryClient = inventoryClient;
 }

 @GetMapping("/{productId}")
 @CircuitBreaker(name="inventoryService", fallbackMethod="fallback")
 public String placeOrder(@PathVariable String productId){
   int stock = inventoryClient.getStock(productId);
   if(stock>0) return "Order placed";
   return "Out of stock";
 }

 public String fallback(String productId, Throwable t){
     return "Fallback: Inventory service down. Try later.";
 }
}
