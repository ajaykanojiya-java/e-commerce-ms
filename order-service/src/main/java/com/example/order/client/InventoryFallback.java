
package com.example.order.client;
import org.springframework.stereotype.Component;

@Component
public class InventoryFallback implements InventoryClient {
 public int getStock(String productId){ return 0; }
}
