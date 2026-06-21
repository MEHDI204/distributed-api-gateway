package com.example.demo.config;

import com.example.demo.security.GatewayAuthorizationFilter;
import com.example.demo.security.GatewaySecurityFilter;
import com.example.demo.security.MonitoringFilter;
import com.example.demo.security.RateLimitingFilter;
import org.springframework.boot.web.servlet.FilterRegistrationBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class FilterConfig {

    // Order 1 — Monitoring wraps everything so latency captures the full lifecycle
    @Bean
    public FilterRegistrationBean<MonitoringFilter> monitoringFilterRegistration(MonitoringFilter monitoringFilter) {
        FilterRegistrationBean<MonitoringFilter> registrationBean = new FilterRegistrationBean<>();
        registrationBean.setFilter(monitoringFilter);
        registrationBean.addUrlPatterns("/*");
        registrationBean.setOrder(1);
        return registrationBean;
    }

    // Order 2 — Rate limit before auth so we shed load even on invalid keys
    @Bean
    public FilterRegistrationBean<RateLimitingFilter> rateLimitingFilterRegistration(RateLimitingFilter rateLimitingFilter) {
        FilterRegistrationBean<RateLimitingFilter> registrationBean = new FilterRegistrationBean<>();
        registrationBean.setFilter(rateLimitingFilter);
        registrationBean.addUrlPatterns("/*");
        registrationBean.setOrder(2);
        return registrationBean;
    }

    // Order 3 — Validate API key
    @Bean
    public FilterRegistrationBean<GatewaySecurityFilter> securityFilterRegistration(GatewaySecurityFilter securityFilter) {
        FilterRegistrationBean<GatewaySecurityFilter> registrationBean = new FilterRegistrationBean<>();
        registrationBean.setFilter(securityFilter);
        registrationBean.addUrlPatterns("/*");
        registrationBean.setOrder(3);
        return registrationBean;
    }

    // Order 4 — Check role permissions after identity is confirmed
    @Bean
    public FilterRegistrationBean<GatewayAuthorizationFilter> authorizationFilterRegistration(GatewayAuthorizationFilter authorizationFilter) {
        FilterRegistrationBean<GatewayAuthorizationFilter> registrationBean = new FilterRegistrationBean<>();
        registrationBean.setFilter(authorizationFilter);
        registrationBean.addUrlPatterns("/*");
        registrationBean.setOrder(4);
        return registrationBean;
    }
}