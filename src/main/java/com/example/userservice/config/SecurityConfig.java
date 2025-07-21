package com.example.userservice.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.web.filter.OncePerRequestFilter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

@Configuration
@EnableWebSecurity
public class SecurityConfig {

    private static final Logger logger = LoggerFactory.getLogger(SecurityConfig.class);

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
            // CSRF 비활성화 - 서비스간 통신을 위해
            .csrf().disable()
            
            // 세션 관리 - STATELESS (마이크로서비스는 상태 없음)
            .sessionManagement(session -> session
                .sessionCreationPolicy(SessionCreationPolicy.STATELESS)
            )
            
            // 요청 로깅 필터 추가
            .addFilterBefore(new RequestLoggingFilter(), org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter.class)
            
            // 모든 요청 허용 - 인증은 Gateway에서 처리
            .authorizeHttpRequests(authz -> authz
                .anyRequest().permitAll()
            );
        
        return http.build();
    }

    public static class RequestLoggingFilter extends OncePerRequestFilter {
        private static final Logger filterLogger = LoggerFactory.getLogger(RequestLoggingFilter.class);

        @Override
        protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, 
                                        FilterChain filterChain) throws ServletException, IOException {
            
            filterLogger.info("=== INCOMING REQUEST ===");
            filterLogger.info("Method: {} {}", request.getMethod(), request.getRequestURI());
            filterLogger.info("Remote IP: {}", request.getRemoteAddr());
            filterLogger.info("Content-Type: {}", request.getHeader("Content-Type"));
            filterLogger.info("User-Agent: {}", request.getHeader("User-Agent"));
            filterLogger.info("X-Forwarded-For: {}", request.getHeader("X-Forwarded-For"));
            filterLogger.info("X-Real-IP: {}", request.getHeader("X-Real-IP"));
            filterLogger.info("Authorization: {}", request.getHeader("Authorization"));
            filterLogger.info("Query String: {}", request.getQueryString());
            
            // 모든 헤더 로깅
            java.util.Enumeration<String> headerNames = request.getHeaderNames();
            filterLogger.info("--- All Headers ---");
            while (headerNames.hasMoreElements()) {
                String headerName = headerNames.nextElement();
                filterLogger.info("{}: {}", headerName, request.getHeader(headerName));
            }
            filterLogger.info("======================");
            
            filterChain.doFilter(request, response);
            
            filterLogger.info("Response Status: {}", response.getStatus());
        }
    }
}