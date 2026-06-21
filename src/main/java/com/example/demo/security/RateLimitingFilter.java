package com.example.demo.security;

import com.example.demo.service.RedisRateLimiterService;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

@Component
public class RateLimitingFilter extends OncePerRequestFilter {

    @Autowired
    private RedisRateLimiterService rateLimiterService;

    public RateLimitingFilter(RedisRateLimiterService rateLimiterService) {
        this.rateLimiterService = rateLimiterService;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain filterChain)
            throws ServletException, IOException {

        // Key on X-API-KEY so each tenant has their own token bucket.
        // Falls back to IP if the header is missing (unauthenticated requests
        // still get rate-limited before the auth filter rejects them).
        String apiKey = request.getHeader("X-API-KEY");
        String rateLimitKey = (apiKey != null && !apiKey.isBlank())
                ? "apikey:" + apiKey
                : "ip:" + request.getRemoteAddr();

        if (rateLimiterService.isAllowed(rateLimitKey)) {
            filterChain.doFilter(request, response);
        } else {
            response.setStatus(HttpStatus.TOO_MANY_REQUESTS.value());
            response.setContentType("application/json");
            response.getWriter().write(
                    "{\"error\": \"Too Many Requests\", " +
                            "\"message\": \"Rate limit exceeded. Please try again later.\"}"
            );
        }
    }
}