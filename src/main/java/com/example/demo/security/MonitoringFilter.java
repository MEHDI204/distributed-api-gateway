package com.example.demo.security;

import com.example.demo.service.MetricsService;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

// Order is defined in FilterConfig — do not add @Order here,
// it conflicts with FilterRegistrationBean ordering.
@Component
public class MonitoringFilter extends OncePerRequestFilter {

    private final MetricsService metricsService;

    public MonitoringFilter(MetricsService metricsService) {
        this.metricsService = metricsService;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {

        long startTime = System.currentTimeMillis();
        metricsService.incrementTotalRequests();

        try {
            filterChain.doFilter(request, response);
        } finally {
            long duration = System.currentTimeMillis() - startTime;
            int status = response.getStatus();

            metricsService.recordStatusCode(status);

            if (status == 429) {
                metricsService.incrementBlockedRequests();
            }

            System.out.printf("[PROD-LOG] Method: %s | Path: %s | Status: %d | Duration: %dms%n",
                    request.getMethod(), request.getRequestURI(), status, duration);
        }
    }
}