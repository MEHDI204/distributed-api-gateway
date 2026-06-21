package com.example.demo.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import java.util.concurrent.TimeUnit;

/**
 * Distributed token-bucket rate limiter backed by Valkey/Redis.
 *
 * Each unique key (API key or IP fallback) gets its own bucket:
 *   MAX_TOKENS    : burst capacity
 *   REFILL_RATE   : tokens added per interval
 *   REFILL_DURATION_MS : interval length in milliseconds
 *
 * State is stored as two Redis keys per client:
 *   rate:tokens:<key>    – current token count
 *   rate:timestamp:<key> – last refill timestamp (epoch ms)
 *
 * Both keys carry a 60-second TTL so idle clients self-evict automatically.
 */
@Service
public class RedisRateLimiterService {

    @Autowired
    private StringRedisTemplate redisTemplate;

    private static final int MAX_TOKENS = 5;
    private static final int REFILL_RATE = 1;
    private static final long REFILL_DURATION_MS = 10_000; // 1 token per 10 seconds
    private static final long TTL_SECONDS = 60;

    /**
     * @param key A namespaced identifier, e.g. "apikey:abc123" or "ip:127.0.0.1".
     *            Callers are responsible for namespacing — this service is key-agnostic.
     * @return true if the request is within the rate limit, false if it should be blocked.
     */
    public boolean isAllowed(String key) {
        String tokensKey    = "rate:tokens:" + key;
        String timestampKey = "rate:timestamp:" + key;

        long now = System.currentTimeMillis();

        String stringTokens    = redisTemplate.opsForValue().get(tokensKey);
        String stringTimestamp = redisTemplate.opsForValue().get(timestampKey);

        int currentTokens;
        long lastRefillTime;

        if (stringTokens == null || stringTimestamp == null) {
            // First request for this key — start with a full bucket
            currentTokens  = MAX_TOKENS;
            lastRefillTime = now;
        } else {
            currentTokens  = Integer.parseInt(stringTokens);
            lastRefillTime = Long.parseLong(stringTimestamp);

            long elapsedTime  = now - lastRefillTime;
            long tokensEarned = (elapsedTime / REFILL_DURATION_MS) * REFILL_RATE;

            if (tokensEarned > 0) {
                currentTokens  = Math.min(MAX_TOKENS, currentTokens + (int) tokensEarned);
                lastRefillTime = now; // advance the refill clock
            }
        }

        if (currentTokens >= 1) {
            currentTokens--;

            redisTemplate.opsForValue().set(tokensKey,    String.valueOf(currentTokens),  TTL_SECONDS, TimeUnit.SECONDS);
            redisTemplate.opsForValue().set(timestampKey, String.valueOf(lastRefillTime), TTL_SECONDS, TimeUnit.SECONDS);

            return true;
        }

        return false;
    }
}