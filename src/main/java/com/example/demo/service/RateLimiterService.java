package com.example.demo.service;

import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.Map;
import java.util.Queue;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentLinkedDeque;

/**
 * ---------------------------------------------------------------
 * V1 REFERENCE IMPLEMENTATION — NOT USED IN PRODUCTION FLOW
 * ---------------------------------------------------------------
 * Single-node, in-memory sliding window rate limiter.
 *
 * Kept for reference to illustrate the evolution from a local
 * ConcurrentHashMap approach to the distributed token-bucket
 * implementation in RedisRateLimiterService.
 *
 * Limitations that motivated the move to Redis:
 *   - State is local to one JVM — multiple gateway instances
 *     each maintain independent windows, so a user can bypass
 *     the limit by hitting different nodes.
 *   - No persistence: state is lost on restart.
 *   - Memory grows unbounded for unique IPs unless eviction is added.
 * ---------------------------------------------------------------
 */
@Service
public class RateLimiterService {

    private final Map<String, Queue<Instant>> limitRequestsMap = new ConcurrentHashMap<>();

    private static final int MAX_REQUESTS = 5;
    private static final long WINDOW_SIZE_IN_SECONDS = 60;

    public boolean isAllowed(String ipAddress) {
        Instant now            = Instant.now();
        Instant windowBoundary = now.minusSeconds(WINDOW_SIZE_IN_SECONDS);

        Queue<Instant> requestTimestamps =
                limitRequestsMap.computeIfAbsent(ipAddress, k -> new ConcurrentLinkedDeque<>());

        // Evict timestamps that have fallen outside the sliding window
        while (!requestTimestamps.isEmpty() && requestTimestamps.peek().isBefore(windowBoundary)) {
            requestTimestamps.poll();
        }

        if (requestTimestamps.size() < MAX_REQUESTS) {
            requestTimestamps.add(now);
            return true;
        }

        return false;
    }
}