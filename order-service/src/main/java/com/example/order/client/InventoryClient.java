
package com.example.order.client;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.*;

@FeignClient(name="inventory-service", fallback=InventoryFallback.class)
public interface InventoryClient {
 @GetMapping("/inventory/{productId}")
 int getStock(@PathVariable("productId") String productId);
}
