package com.example.demo.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

@Service
public class GatewayAuthService {

    @Autowired
    private StringRedisTemplate redisTemplate;

    private static final String KEYS_HASH_NAME = "gateway:keys";

    /**
     * Looks up an API key in Valkey and returns the associated role.
     * @param apiKey The incoming header token from the client
     * @return "ADMIN", "USER", or null if the key is invalid
     */
    public String getRoleForKey(String apiKey) {
        if (apiKey == null || apiKey.trim().isEmpty()) {
            return null;
        }

        // Fetch the field value from the Valkey Hash
        Object role = redisTemplate.opsForHash().get(KEYS_HASH_NAME, apiKey);

        // If the key wasn't found, role will be null.
        // Otherwise, we convert it cleanly to a String.
        return role != null ? role.toString() : null;
    }
}