package com.example.demo.security;

import com.example.demo.service.GatewayAuthService;
import jakarta.servlet.*;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

@Component
public class GatewaySecurityFilter extends OncePerRequestFilter {

    @Autowired
    private GatewayAuthService authService;

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain filterChain) throws ServletException, IOException {

        String apiKey = request.getHeader("X-API-KEY");

        String role = authService.getRoleForKey(apiKey);

        if (role == null) {
            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED); // HTTP 401
            response.setContentType("application/json");
            response.getWriter().write("{\"error\": \"Invalid or missing API Key. Access Denied.\"}");
            return; // Stop the filter chain execution right here!
        }

        request.setAttribute("userRole", role);

        filterChain.doFilter(request, response);
    }
}