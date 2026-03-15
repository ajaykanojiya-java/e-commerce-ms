# E-Commerce Microservices

This project is a microservices-based e-commerce application built with Spring Boot, Spring Cloud, and other technologies. It includes the following services:

- **Eureka Server**: Service discovery server.
- **API Gateway**: Gateway for routing requests to microservices.
- **Order Service**: Handles order management.
- **Inventory Service**: Manages inventory.
- **Payment Service**: Processes payments.

## Prerequisites

- Java 17 or higher
- Maven 3.6 or higher (or use the included Maven wrapper)
- Git

## Getting Started

1. **Clone the repository**:
   ```
   git clone https://github.com/ajaykanojiya-java/e-commerce-ms.git
   cd e-commerce-ms
   ```

2. **Build the project** (optional, as services use Maven wrapper):
   ```
   ./mvnw clean install
   ```

## Starting the Application

To start all microservices in the correct order:

1. Run the start script:
   - On Windows: Double-click `start-all-services.bat` or run it in the terminal.
   - Alternatively, run: `powershell -ExecutionPolicy Bypass -File start-all-services.ps1`

   This will start the services in the following order:
   - Eureka Server (port 8761)
   - API Gateway (port 8080)
   - Order Service (port 8081)
   - Inventory Service (port 8082)
   - Payment Service (port 8083)

2. Wait for all services to start. Check the command windows for logs.

3. Access the services:
   - Eureka Dashboard: http://localhost:8761
   - API Gateway: http://localhost:8080

## Stopping the Application

To stop all running services:

1. Run the stop script:
   - On Windows: Double-click `stop-all-services.bat` or run it in the terminal.
   - Alternatively, run: `powershell -ExecutionPolicy Bypass -File stop-all-services.ps1`

   This will terminate all processes listening on the service ports.

## Manual Start/Stop

If you prefer to start services manually:

- Start Eureka Server: `cd eureka-server && ./mvnw spring-boot:run`
- Start API Gateway: `cd apigateway && ./mvnw spring-boot:run`
- And so on for other services.

To stop, close the terminal windows or use `Ctrl+C`.

## Ports Used

- Eureka Server: 8761
- API Gateway: 8080
- Order Service: 8081
- Inventory Service: 8082
- Payment Service: 8083

## Troubleshooting

- If ports are in use, run the stop script or manually kill processes using `netstat -ano | findstr :PORT` and `taskkill /PID <PID> /F`.
- Ensure Java and Maven are installed and in PATH, or use the wrappers.

## Contributing

1. Fork the repository.
2. Create a feature branch.
3. Commit your changes.
4. Push to the branch.
5. Open a Pull Request.
