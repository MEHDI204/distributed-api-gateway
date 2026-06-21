package com.example.demo.service;

import org.springframework.stereotype.Service;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;

@Service
public class MetricsService {

    private final AtomicLong totalRequests = new AtomicLong(0);
    private final AtomicLong blockedRequests = new AtomicLong(0);
    private final ConcurrentHashMap<Integer, AtomicLong> statusCodeCounts = new ConcurrentHashMap<>();
    private final AtomicLong criticalErrors = new AtomicLong(0);

    public void incrementCriticalErrors() {
        criticalErrors.incrementAndGet();
    }

    public long getCriticalErrors() {
        return criticalErrors.get();
    }

    public void incrementTotalRequests() {
        totalRequests.incrementAndGet();
    }

    public void incrementBlockedRequests() {
        blockedRequests.incrementAndGet();
    }

    public void recordStatusCode(int statusCode) {
        statusCodeCounts.computeIfAbsent(statusCode, k -> new AtomicLong(0)).incrementAndGet();
    }

    public long getTotalRequests() {
        return totalRequests.get();
    }

    public long getBlockedRequests() {
        return blockedRequests.get();
    }

    public ConcurrentHashMap<Integer, Long> getStatusCodeSnapshot() {
        ConcurrentHashMap<Integer, Long> snapshot = new ConcurrentHashMap<>();
        statusCodeCounts.forEach((code, count) -> snapshot.put(code, count.get()));
        return snapshot;
    }
}
