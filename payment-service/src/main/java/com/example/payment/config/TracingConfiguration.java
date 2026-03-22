package com.example.payment.config;

import io.micrometer.tracing.Tracer;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.filter.OncePerRequestFilter;
import org.springframework.boot.web.servlet.FilterRegistrationBean;
import org.springframework.context.annotation.Bean;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.MDC;
import java.io.IOException;
import java.util.Optional;

/**
 * Configuration for distributed tracing in Payment Service
 * Ensures trace context is properly populated in MDC for logging
 */
@Configuration
public class TracingConfiguration {
    
    @Bean
    public FilterRegistrationBean<TracingFilter> tracingFilter(Optional<Tracer> tracer) {
        FilterRegistrationBean<TracingFilter> bean = new FilterRegistrationBean<>();
        bean.setFilter(new TracingFilter(tracer.orElse(null)));
        bean.setOrder(1);
        return bean;
    }
    
    static class TracingFilter extends OncePerRequestFilter {
        private final Tracer tracer;
        
        public TracingFilter(Tracer tracer) {
            this.tracer = tracer;
        }
        
        @Override
        protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain) 
                throws ServletException, IOException {
            try {
                // Populate MDC with trace and span IDs
                if (tracer != null && tracer.currentTraceContext() != null && tracer.currentTraceContext().context() != null) {
                    String traceId = tracer.currentTraceContext().context().traceId();
                    String spanId = tracer.currentTraceContext().context().spanId();
                    
                    if (traceId != null && !traceId.isEmpty()) {
                        MDC.put("traceId", traceId);
                    }
                    if (spanId != null && !spanId.isEmpty()) {
                        MDC.put("spanId", spanId);
                    }
                }
                
                filterChain.doFilter(request, response);
            } finally {
                MDC.remove("traceId");
                MDC.remove("spanId");
            }
        }
    }
}

