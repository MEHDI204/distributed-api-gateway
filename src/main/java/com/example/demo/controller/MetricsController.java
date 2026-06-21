package com.example.demo.controller;

import com.example.demo.service.MetricsService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.Map;

@RestController
public class MetricsController {

    private final MetricsService metricsService;

    public MetricsController(MetricsService metricsService) {
        this.metricsService = metricsService;
    }

    @GetMapping("/gateway/metrics")
    public ResponseEntity<Map<String, Object>> getMetrics() {
        Map<String, Object> metrics = new HashMap<>();

        metrics.put("totalRequests", metricsService.getTotalRequests());
        metrics.put("blockedRequests", metricsService.getBlockedRequests());
        metrics.put("statusCodes", metricsService.getStatusCodeSnapshot());
        metrics.put("criticalErrors", metricsService.getCriticalErrors());

        // Calculate a basic health metric
        long total = metricsService.getTotalRequests();
        long blocked = metricsService.getBlockedRequests();
        double blockRate = total == 0 ? 0.0 : ((double) blocked / total) * 100;

        metrics.put("blockRatePercentage", String.format("%.2f%%", blockRate));

        return ResponseEntity.ok(metrics);
    }
}
