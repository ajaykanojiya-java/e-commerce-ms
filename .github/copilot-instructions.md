# E-Commerce Microservices - Copilot Instructions

## Project Overview

This is a **Spring Boot microservices-based e-commerce application** demonstrating cloud-native architecture patterns including service discovery, API gateway routing, inter-service communication with circuit breakers, and resilience patterns.

### Technology Stack
- **Framework**: Spring Boot 3.3.5
- **Spring Cloud**: 2024.0.2
- **Java**: Version 21
- **Build Tool**: Maven 3.6+ (with Maven Wrapper)
- **Service Discovery**: Netflix Eureka
- **API Gateway**: Spring Cloud Gateway
- **Inter-service Communication**: OpenFeign with Fallback pattern
- **Resilience Pattern**: Resilience4j Circuit Breaker
- **Database**: H2 (In-memory for Inventory Service)
- **IDE**: JetBrains IntelliJ IDEA

---

## Architecture & Service Details

### System Architecture Flow
```
Client → API Gateway (8080) → Service Discovery (Eureka)
         ↓
    ├─ Order Service (8081) → Inventory Service (8082) → Payment Service (8083)
```

### 1. **Eureka Server (Service Registry)**
   - **Port**: 8761
   - **Purpose**: Centralized service discovery and registration
   - **Location**: `eureka-server/`
   - **Main Class**: `EurekaServerApplication.java`
   - **Key Config**: `application.yml` - Disabled self-registration
   - **Annotations**: `@EnableEurekaServer`
   - **Use Cases**:
     - All microservices register themselves on startup
     - Services discover each other dynamically
     - Load balancing across service instances
   - **Eureka Dashboard**: http://localhost:8761

### 2. **API Gateway (Request Router)**
   - **Port**: 8080
   - **Purpose**: Single entry point for all client requests; routes to appropriate services
   - **Location**: `apigateway/`
   - **Main Class**: `ApigatewayApplication.java`
   - **Key Config**: `application.yml` with route predicates
   - **Dependencies**: Spring Cloud Gateway, Eureka Client
   - **Routing Rules**:
     - `/order/**` → order-service
     - `/inventory/**` → inventory-service
     - `/payment/**` → payment-service
   - **Features**:
     - Discovery client enabled for service lookup
     - Load balancing built-in (via `lb://service-name`)
   - **Annotations**: `@EnableDiscoveryClient`

### 3. **Order Service**
   - **Port**: 8081
   - **Purpose**: Handles order placement and management
   - **Location**: `order-service/`
   - **Main Class**: `OrderApplication.java`
   - **Key Components**:
     - **Controller**: `OrderController.java`
       - Endpoint: `GET /order/{productId}` - Place order for a product
       - Circuit Breaker: Calls inventory-service with fallback
     - **Feign Client**: `InventoryClient.java`
       - Remote call: `GET /inventory/{productId}`
       - Fallback: `InventoryFallback.java` - Returns default on service failure
   - **Dependencies**: Eureka Client, OpenFeign, Resilience4j
   - **Circuit Breaker Config**:
     - Sliding window size: 5
     - Failure rate threshold: 50%
   - **Annotations**: `@EnableFeignClients`, `@CircuitBreaker`, `@FeignClient`
   - **Resilience Pattern**: 
     - Uses Feign + Circuit Breaker to call inventory-service
     - Fallback method triggered when inventory-service is down

### 4. **Inventory Service**
   - **Port**: 8082
   - **Purpose**: Manages product inventory and stock levels
   - **Location**: `inventory-service/`
   - **Main Class**: `InventoryApplication.java`
   - **Key Components**:
     - **Controller**: `InventoryController.java`
       - Endpoint: `GET /inventory/{productId}` - Get stock for product
       - Calls payment-service before returning stock
       - Circuit Breaker: Handles payment-service failures gracefully
     - **Feign Client**: `PaymentClient.java`
       - Remote call: `POST /payment/pay/{productId}`
       - Fallback: `PaymentFallback.java`
   - **Database**: H2 (In-memory)
   - **Configuration**:
     - JPA/Hibernate enabled
     - H2 console available at `/h2-console` (if enabled)
   - **Dependencies**: Eureka Client, OpenFeign, Data JPA, H2, Resilience4j
   - **Circuit Breaker Config**:
     - Sliding window size: 5
     - Failure rate threshold: 50%
   - **Annotations**: `@EnableFeignClients`, `@CircuitBreaker`

### 5. **Payment Service**
   - **Port**: 8083
   - **Purpose**: Processes payments for orders
   - **Location**: `payment-service/`
   - **Main Class**: `PaymentApplication.java`
   - **Key Components**:
     - **Controller**: `PaymentController.java`
       - Endpoint: `POST /payment/pay/{productID}` - Process payment
       - Logic: Returns success for all products except product ID "1"
       - Throws exception for productID=1 (simulated payment failure)
   - **Dependencies**: Spring Boot Web, Eureka Client
   - **Annotations**: `@SpringBootApplication`
   - **Testing**: Use this service to test circuit breaker behavior
     - Valid products: 2, 3, 4, ... → Payment Success
     - Invalid product: 1 → Payment Failure (triggers fallback)

---

## Project Structure

```
e-commerce-ms/
├── pom.xml                          # Root parent POM - centralized dependency management
├── README.md                        # User-facing documentation
├── copilot-instructions.md          # This file - AI assistant guidelines
├── start-all-services.bat/.ps1      # Scripts to start all services
├── stop-all-services.bat/.ps1       # Scripts to stop all services
│
├── eureka-server/
│   ├── pom.xml
│   └── src/main/
│       ├── java/com/example/EurekaServerApplication.java
│       └── resources/application.yml
│
├── apigateway/
│   ├── pom.xml
│   └── src/main/
│       ├── java/com/example/apigateway/ApigatewayApplication.java
│       └── resources/application.yml
│
├── order-service/
│   ├── pom.xml
│   └── src/main/
│       ├── java/com/example/order/
│       │   ├── OrderApplication.java
│       │   ├── controller/OrderController.java
│       │   └── client/InventoryClient.java + InventoryFallback.java
│       └── resources/application.yml
│
├── inventory-service/
│   ├── pom.xml
│   └── src/main/
│       ├── java/com/example/inventory/
│       │   ├── InventoryApplication.java
│       │   ├── controller/InventoryController.java
│       │   └── client/PaymentClient.java + PaymentFallback.java
│       └── resources/application.yml
│
└── payment-service/
    ├── pom.xml
    └── src/main/
        ├── java/com/example/payment/
        │   ├── PaymentApplication.java
        │   └── controller/PaymentController.java
        └── resources/application.yml
```

---

## Build & Dependency Management

### Maven Structure
- **Root POM** (`pom.xml`):
  - Parent: Spring Boot Starter Parent 3.2.0
  - Module declarations (all 5 services)
  - Centralized properties: `java.version`, `spring-cloud.version`
  - Centralized `dependencyManagement` for Spring Cloud BOM import
  - **Result**: All child modules inherit versions from root - NO duplication

- **Child POMs** (each service):
  - Inherit from root parent
  - Specify only service-specific dependencies
  - Include Spring Boot Maven Plugin for building executable JARs
  - No redundant `dependencyManagement` blocks

### Key Dependencies
```
- spring-boot-starter-parent: 3.3.5 (inherited)
- spring-cloud-dependencies: 2024.0.2 (BOM imported)
- spring-cloud-starter-netflix-eureka-server (eureka-server)
- spring-cloud-starter-gateway (apigateway)
- spring-cloud-starter-openfeign (order-service, inventory-service)
- spring-cloud-starter-circuitbreaker-resilience4j (circuit breaker pattern)
- spring-boot-starter-data-jpa, h2database (inventory-service database)
- resilience4j-spring-boot3 (resilience pattern support)
```

### Build Commands
```powershell
# Build entire project from root
.\mvnw.cmd clean install

# Build specific module
cd order-service
..\mvnw.cmd clean package

# Skip tests
.\mvnw.cmd clean install -DskipTests

# Run specific service
cd order-service
..\mvnw.cmd spring-boot:run
```

---

## Service Communication Flow

### Example: Order Placement Flow
```
1. Client: GET /order/2 (via API Gateway)
2. API Gateway routes to → Order Service (8081)
3. OrderController.placeOrder() checks stock
4. Creates InventoryClient (Feign) → calls Inventory Service (8082)
5. InventoryController.getStock() receives request
6. Calls PaymentClient (Feign) → Payment Service (8083)
7. PaymentController.pay() processes payment
   - Product ID "1": Throws exception (failure case)
   - Other IDs: Returns "Payment Success"
8. InventoryController receives response:
   - Success: returns stock count (10)
   - Failure: Circuit breaker triggers fallback → returns -1
9. OrderController receives response:
   - Stock > 0: Returns "Order placed"
   - Stock = 0 or -1: Returns "Out of stock" or fallback message
10. Response sent back through API Gateway to Client
```

### Resilience4j Circuit Breaker Behavior
- **Order Service → Inventory Service**:
  - Threshold: 50% failure rate, Window size: 5 requests
  - Fallback: "Fallback: Inventory service down. Try later."
  
- **Inventory Service → Payment Service**:
  - Threshold: 50% failure rate, Window size: 5 requests
  - Fallback: Returns -1 (indicates stock unavailable)

---

## Configuration Files

### Eureka Server (application.yml)
```yaml
server:
  port: 8761
eureka:
  client:
    register-with-eureka: false  # Don't register itself
    fetch-registry: false         # Don't fetch registry
```

### API Gateway (application.yml)
```yaml
server:
  port: 8080
spring:
  application:
    name: api-gateway
  cloud:
    gateway:
      routes:
        - id: order-service
          uri: lb://order-service
          predicates:
            - Path=/order/**
```

### Order Service (application.yml)
```yaml
server:
  port: 8081
spring:
  application:
    name: order-service
eureka:
  client:
    service-url:
      defaultZone: http://localhost:8761/eureka
resilience4j:
  circuitbreaker:
    instances:
      inventoryService:
        slidingWindowSize: 5
        failureRateThreshold: 50
```

### Inventory Service (application.yml)
```yaml
server:
  port: 8082
spring:
  datasource:
    url: jdbc:h2:mem:inventorydb  # In-memory H2 database
  jpa:
    database-platform: org.hibernate.dialect.H2Dialect
eureka:
  client:
    service-url:
      defaultZone: http://localhost:8761/eureka
```

### Payment Service (application.yml)
```yaml
server:
  port: 8083
spring:
  application:
    name: payment-service
eureka:
  client:
    service-url:
      defaultZone: http://localhost:8761/eureka
```

---

## Testing & Troubleshooting

### Starting the Application

**Option 1: Automated Script**
```powershell
# Windows
start-all-services.bat

# Or via PowerShell
powershell -ExecutionPolicy Bypass -File start-all-services.ps1
```

**Option 2: Manual Start** (terminal windows open for each)
```powershell
# Terminal 1: Eureka Server
cd eureka-server
..\mvnw.cmd spring-boot:run

# Terminal 2: API Gateway
cd apigateway
..\mvnw.cmd spring-boot:run

# Terminal 3: Order Service
cd order-service
..\mvnw.cmd spring-boot:run

# Terminal 4: Inventory Service
cd inventory-service
..\mvnw.cmd spring-boot:run

# Terminal 5: Payment Service
cd payment-service
..\mvnw.cmd spring-boot:run
```

### Testing Endpoints

```powershell
# Via Postman or curl

# 1. Check Eureka Dashboard
GET http://localhost:8761

# 2. Place order (via API Gateway)
GET http://localhost:8080/order/2

# 3. Direct inventory check
GET http://localhost:8082/inventory/2

# 4. Test payment service directly
POST http://localhost:8083/payment/pay/2
Content-Type: application/json

# 5. Test circuit breaker (product ID 1 causes payment failure)
GET http://localhost:8080/order/1
# Expected: Fallback response or circuit breaker message
```

### Expected Responses

**Success Case (Product ID > 1)**
```
GET /order/2 
→ Order Service calls Inventory Service
  → Inventory Service calls Payment Service (success)
  → Returns stock: 10
→ Response: "Order placed"
```

**Failure Case (Product ID = 1)**
```
GET /order/1
→ Order Service calls Inventory Service
  → Inventory Service calls Payment Service (throws exception)
  → Circuit breaker intercepts failure
  → Fallback returns -1 (stock unavailable)
→ Response: "Out of stock" or fallback message
```

### Troubleshooting

| Issue | Solution |
|-------|----------|
| **Port already in use** | Run stop scripts or `netstat -ano \| findstr :PORT` then `taskkill /PID <id> /F` |
| **Services not registering in Eureka** | Verify all services have correct `eureka.client.service-url` pointing to Eureka server |
| **Feign client call fails** | Check `@FeignClient` name matches service application name, verify Eureka registration |
| **Circuit breaker not triggering** | Ensure `@CircuitBreaker` annotation is on the method, verify resilience4j config |
| **H2 database not accessible** | Inventory Service uses in-memory H2; data is lost on service restart |
| **Maven build fails** | Ensure Java 17+, run `.\mvnw.cmd clean install` from root directory |

---

## Development Guidelines for Copilot

### Code Style & Standards
- **Package naming**: `com.example.{service-name}.{layer}`
  - Layers: `controller`, `client`, `service`, `entity`, `config`
- **Naming conventions**:
  - Controllers: `*Controller.java`
  - Feign Clients: `*Client.java`
  - Fallback implementations: `*Fallback.java`
  - DTOs/Models: `*Request.java`, `*Response.java`, `*DTO.java`
- **Annotations**:
  - Services: `@Service`
  - Controllers: `@RestController`, `@RequestMapping`
  - Clients: `@FeignClient(name="service-name", fallback=FallbackClass.class)`
  - Resilience: `@CircuitBreaker(name="instanceName", fallbackMethod="fallbackMethod")`

### Common Tasks & Code Patterns

#### 1. Adding New Endpoint to a Service
```java
@RestController
@RequestMapping("/resource")
public class ResourceController {
    
    @GetMapping("/{id}")
    public ResponseEntity<Resource> getResource(@PathVariable Long id) {
        // Implementation
        return ResponseEntity.ok(resource);
    }
}
```

#### 2. Creating Feign Client with Fallback
```java
// Client interface
@FeignClient(name="target-service", fallback=TargetServiceFallback.class)
public interface TargetServiceClient {
    @GetMapping("/endpoint/{id}")
    String callService(@PathVariable("id") Long id);
}

// Fallback implementation
@Component
public class TargetServiceFallback implements TargetServiceClient {
    @Override
    public String callService(Long id) {
        return "Service unavailable";
    }
}
```

#### 3. Applying Circuit Breaker
```java
@GetMapping("/operation/{id}")
@CircuitBreaker(name="myCircuit", fallbackMethod="fallbackOperation")
public String performOperation(@PathVariable String id) {
    return externalServiceClient.call(id);
}

public String fallbackOperation(String id, Throwable t) {
    return "Operation failed, using fallback";
}
```

#### 4. Adding Database Entity (Inventory Service)
```java
@Entity
@Table(name = "products")
public class Product {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    private String name;
    private int stock;
    // Getters and setters
}
```

### Best Practices
1. **Always use Eureka service names** in Feign clients: `@FeignClient(name="order-service")`
2. **Implement fallback methods** for all Feign clients to handle service failures
3. **Configure circuit breaker thresholds** based on SLA requirements
4. **Use load balancing** prefix in gateway routes: `uri: lb://service-name`
5. **Centralize configuration** in application.yml files
6. **Log key operations** for debugging distributed systems
7. **Test circuit breaker scenarios** explicitly
8. **Use consistent versioning** across all services

### Adding New Microservice

When adding a new service to the architecture:

1. **Create directory**: `new-service/`
2. **Create pom.xml**:
   ```xml
   <parent>
       <groupId>com.restaurant</groupId>
       <artifactId>e-commerce-ms</artifactId>
       <version>1.0</version>
   </parent>
   <artifactId>new-service</artifactId>
   ```
3. **Add to root pom.xml** `<modules>` section
4. **Configure application.yml** with port, service name, Eureka settings
5. **Create @SpringBootApplication** main class with appropriate annotations
6. **Add routes** to API Gateway for new service endpoints
7. **Implement controllers** and integrate with other services via Feign clients

---

## Important Notes for Copilot

### What to Watch For
- **Circular dependencies**: Ensure services don't call each other in circles (Order → Inventory → Payment is OK)
- **Hardcoded service URLs**: Always use Eureka service names, never hardcode localhost
- **Missing fallback methods**: Every Feign client must have a fallback strategy
- **Port conflicts**: Services must run on different ports (8761, 8080, 8081, 8082, 8083)
- **Eureka registration delays**: Services may take 5-10 seconds to appear in Eureka dashboard

### Performance Considerations
- Circuit breaker sliding window size of 5 is small; adjust for production
- H2 database is in-memory only (Inventory Service); implement persistent DB for production
- No authentication/authorization implemented; add Spring Security for production
- No API rate limiting; consider adding rate limiters to API Gateway

### Security Reminders
- **No JWT/OAuth2**: This is a demo; implement for production
- **No input validation**: Add `@Valid` and validation annotations
- **No request encryption**: Configure HTTPS/TLS for production
- **Eureka is open**: Secure Eureka server with authentication

---

## References & Resources

- [Spring Boot Documentation](https://spring.io/projects/spring-boot)
- [Spring Cloud Documentation](https://spring.io/projects/spring-cloud)
- [Netflix Eureka](https://github.com/Netflix/eureka)
- [Spring Cloud Gateway](https://spring.io/projects/spring-cloud-gateway)
- [OpenFeign Documentation](https://spring.io/projects/spring-cloud-openfeign)
- [Resilience4j Documentation](https://resilience4j.readme.io/)
- [Maven Documentation](https://maven.apache.org/guides/)

---

## Version History

- **v1.2** (Current): Upgraded to Spring Boot 3.3.5, Spring Cloud 2024.0.2 (latest stable versions with Java 21)
- **v1.1**: Upgraded to Java 21, Spring Cloud 2023.0.3 (stable patch)
- **v1.0**: Initial microservices architecture with 5 services, Eureka discovery, API Gateway, Feign clients, and Resilience4j circuit breakers
- **Optimizations**: Centralized dependency management in root POM, eliminated duplication across all modules

---

**Last Updated**: March 22, 2026  
**Maintained By**: Development Team  
**Project Status**: Active Development

