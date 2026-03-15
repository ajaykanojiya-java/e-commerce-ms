# To run this script, use: powershell -ExecutionPolicy Bypass -File E:\e-commerce-ms\start-all-services.ps1
# PowerShell script to start all microservices in order
# Run this script from the root directory E:\e-commerce-ms

# Function to start a service
function Start-Service {
    param (
        [string]$moduleName
    )
    $path = Join-Path $PSScriptRoot $moduleName
    Write-Host "Starting $moduleName..."
    Start-Process -FilePath "cmd.exe" -ArgumentList "/c cd /d $path && .\mvnw.cmd spring-boot:run" -WindowStyle Normal
}

# Start Eureka Server first
Start-Service "eureka-server"

# Wait for Eureka to be ready
Write-Host "Waiting for Eureka Server to be ready..."
do {
    $connected = Test-NetConnection -ComputerName localhost -Port 8761 -InformationLevel Quiet
    if ($connected) {
        Write-Host "Eureka Server is ready."
        break
    } else {
        Write-Host "Eureka not ready yet, waiting..."
        Start-Sleep -Seconds 5
    }
} while ($true)

# Start API Gateway
Start-Service "apigateway"
Start-Sleep -Seconds 10

# Start Order Service
Start-Service "order-service"
Start-Sleep -Seconds 10

# Start Inventory Service
Start-Service "inventory-service"
Start-Sleep -Seconds 10

# Start Payment Service
Start-Service "payment-service"

Write-Host "All services are starting. Check the command windows for logs."
