package com.example.demo.security;

import jakarta.servlet.*;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

@Component
public class GatewayAuthorizationFilter extends OncePerRequestFilter {

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain filterChain) throws ServletException, IOException {

        String role = (String) request.getAttribute("userRole");

        String method = request.getMethod();
        String path = request.getRequestURI();

        if (("POST".equalsIgnoreCase(method) || "PUT".equalsIgnoreCase(method) || "DELETE".equalsIgnoreCase(method))
                && !"ADMIN".equals(role)) {

            response.setStatus(HttpServletResponse.SC_FORBIDDEN); // HTTP 403
            response.setContentType("application/json");
            response.getWriter().write("{\"error\": \"Forbidden. You do not have ADMIN clearance for this operation.\"}");
            return; // Short-circuit and stop execution right here!
        }

        filterChain.doFilter(request, response);
    }
}