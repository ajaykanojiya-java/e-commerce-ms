package com.example.inventory.util;

import io.micrometer.tracing.Tracer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 * Utility class for distributed tracing in Inventory Service
 * Provides methods to log with trace and span IDs
 */
@Component
public class TracingUtil {
    private static final Logger logger = LoggerFactory.getLogger(TracingUtil.class);
    
    @Autowired(required = false)
    private Tracer tracer;
    
    private String getTraceId() {
        // First try to get from Tracer's current trace context
        if (tracer != null && tracer.currentTraceContext() != null && tracer.currentTraceContext().context() != null) {
            String traceId = tracer.currentTraceContext().context().traceId();
            if (traceId != null && !traceId.isEmpty()) {
                return traceId;
            }
        }
        
        // Fall back to MDC
        String mdc = MDC.get("traceId");
        if (mdc != null && !mdc.isEmpty()) {
            return mdc;
        }
        
        // Check for common MDC keys that Micrometer might use
        mdc = MDC.get("X-B3-TraceId");
        if (mdc != null && !mdc.isEmpty()) {
            return mdc;
        }
        
        return "UNKNOWN";
    }
    
    private String getSpanId() {
        // First try to get from Tracer's current span
        if (tracer != null && tracer.currentSpan() != null && tracer.currentSpan().context() != null) {
            String spanId = tracer.currentSpan().context().spanId();
            if (spanId != null && !spanId.isEmpty()) {
                return spanId;
            }
        }
        
        // Fall back to MDC
        String mdc = MDC.get("spanId");
        if (mdc != null && !mdc.isEmpty()) {
            return mdc;
        }
        
        // Check for common MDC keys that Micrometer might use
        mdc = MDC.get("X-B3-SpanId");
        if (mdc != null && !mdc.isEmpty()) {
            return mdc;
        }
        
        return "UNKNOWN";
    }
    
    public void logInfo(String serviceName, String operation, String message) {
        String traceId = getTraceId();
        String spanId = getSpanId();
        logger.info("[{}-{}] [{}] [{}] {}", traceId, spanId, serviceName, operation, message);
    }
    
    public void logError(String serviceName, String operation, String message, Exception ex) {
        String traceId = getTraceId();
        String spanId = getSpanId();
        logger.error("[{}-{}] [{}] [{}] {} - Error: {}", traceId, spanId, serviceName, operation, message, ex.getMessage(), ex);
    }
    
    public void logDebug(String serviceName, String operation, String message) {
        String traceId = getTraceId();
        String spanId = getSpanId();
        logger.debug("[{}-{}] [{}] [{}] {}", traceId, spanId, serviceName, operation, message);
    }
}

