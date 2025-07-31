package com.example.userservice.config;

import com.example.userservice.security.HeaderBasedAuthenticationFilter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
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
            
            // 헤더 기반 인증 필터 추가
            .addFilterBefore(new HeaderBasedAuthenticationFilter(), UsernamePasswordAuthenticationFilter.class)
            
            // 요청 로깅 필터 추가
            .addFilterBefore(new RequestLoggingFilter(), HeaderBasedAuthenticationFilter.class)
            
            // 엔드포인트별 권한 설정
            .authorizeHttpRequests(authz -> authz
                // Health check - 모두 허용
                .antMatchers(HttpMethod.GET, "/api/users/health").permitAll()
                .antMatchers(HttpMethod.GET, "/api/health").permitAll()
                
                // Gateway internal endpoints - 모두 허용
                .antMatchers(HttpMethod.GET, "/api/users/gateway/lookup/*").permitAll()
                
                // 인증 관련 - 모두 허용
                .antMatchers(HttpMethod.POST, "/api/users").permitAll()  // 회원가입
                .antMatchers(HttpMethod.POST, "/api/users/authenticate").permitAll()  // 로그인
                .antMatchers(HttpMethod.POST, "/api/users/oauth2").permitAll()  // OAuth2 로그인
                .antMatchers(HttpMethod.PUT, "/api/users/verify-email").permitAll()  // 이메일 인증
                
                // 사용자 조회 - 인증 필요
                .antMatchers(HttpMethod.GET, "/api/users").authenticated()  // 전체 사용자 목록 (ADMIN 권한 필요할 수도)
                .antMatchers(HttpMethod.GET, "/api/users/*").authenticated()  // 특정 사용자 조회
                .antMatchers(HttpMethod.GET, "/api/users/email/*").authenticated()  // 이메일로 사용자 조회
                
                // 사용자 수정/삭제 - 인증 필요 (본인 또는 ADMIN)
                .antMatchers(HttpMethod.PUT, "/api/users/*").authenticated()
                .antMatchers(HttpMethod.DELETE, "/api/users/*").authenticated()
                
                // 관리자 기능 - ADMIN 권한 필요
                .antMatchers(HttpMethod.PUT, "/api/users/*/suspend").hasRole("ADMIN")
                .antMatchers(HttpMethod.PUT, "/api/users/*/activate").hasRole("ADMIN")
                .antMatchers(HttpMethod.POST, "/api/users/test-event").hasRole("ADMIN")  // 테스트용 엔드포인트
                
                // 기타 모든 요청은 인증 필요
                .anyRequest().authenticated()
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