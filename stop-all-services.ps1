# PowerShell script to stop all microservices
# Run this script from the root directory E:\e-commerce-ms

$ports = @(8761, 8081, 8084, 8082, 8083)

foreach ($port in $ports) {
    Write-Host "Stopping service on port $port..."
    $line = netstat -ano | findstr ":$port "
    if ($line) {
        $parts = $line -split '\s+'
        $pid = $parts[-1]
        taskkill /PID $pid /F
        Write-Host "Stopped process $pid on port $port."
    } else {
        Write-Host "No process found on port $port."
    }
}

Write-Host "All services stopped."
