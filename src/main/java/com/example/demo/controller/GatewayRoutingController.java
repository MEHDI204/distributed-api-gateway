package com.example.demo.controller;

import com.example.demo.service.MetricsService;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.client.RestTemplate;

@RestController
public class GatewayRoutingController {

    private final RestTemplate restTemplate = new RestTemplate();
    private final MetricsService metricsService;

    @Value("${gateway.downstream.url}")
    private String downstreamUrl;

    public GatewayRoutingController(MetricsService metricsService) {
        this.metricsService = metricsService;
    }

    @GetMapping("/gateway/github/**")
    public ResponseEntity<String> routeRequest(HttpServletRequest request) {
        String fullPath = request.getRequestURI();
        String subPath = fullPath.substring("/gateway/github".length());

        String targetUrl = downstreamUrl + subPath;
        System.out.println("DEBUG: Gateway is forwarding request to: " + targetUrl);

        String apiKey = request.getHeader("X-API-KEY");

        org.springframework.http.HttpHeaders headers = new org.springframework.http.HttpHeaders();
        headers.set("X-API-KEY", apiKey);
        org.springframework.http.HttpEntity<Void> entity = new org.springframework.http.HttpEntity<>(headers);

        try {
            org.springframework.http.ResponseEntity<String> response = restTemplate.exchange(
                    targetUrl,
                    org.springframework.http.HttpMethod.GET,
                    entity,
                    String.class
            );
            return ResponseEntity.ok(response.getBody());
        } catch (Exception e) {
            metricsService.incrementCriticalErrors();
            return ResponseEntity.status(502)
                    .body("{\"error\": \"Bad Gateway. Failed to reach local backend service. Exception: " + e.getMessage() + "\"}");
        }
    }

    @GetMapping("/mock-backend/**")
    public ResponseEntity<String> mockBackend(HttpServletRequest request) {
        return ResponseEntity.ok("{\"message\": \"Success! Distributed simulation working locally.\"}");
    }
}