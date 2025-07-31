package com.example.userservice.security;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.filter.OncePerRequestFilter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.UUID;
import java.util.stream.Collectors;

public class HeaderBasedAuthenticationFilter extends OncePerRequestFilter {
    
    private static final Logger logger = LoggerFactory.getLogger(HeaderBasedAuthenticationFilter.class);
    private static final String USER_ID_HEADER = "X-User-Id";
    private static final String USER_EMAIL_HEADER = "X-User-Email";
    private static final String USER_ROLES_HEADER = "X-User-Roles";

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, 
                                    FilterChain filterChain) throws ServletException, IOException {
        
        String userIdStr = request.getHeader(USER_ID_HEADER);
        String userEmail = request.getHeader(USER_EMAIL_HEADER);
        String userRoles = request.getHeader(USER_ROLES_HEADER);
        
        logger.debug("Processing authentication headers - ID: {}, Email: {}, Roles: {}", 
            userIdStr, userEmail, userRoles);
        
        if (userEmail != null && !userEmail.trim().isEmpty()) {
            Collection<GrantedAuthority> authorities = parseRoles(userRoles);
            
            Authentication authentication;
            if (userIdStr != null && !userIdStr.trim().isEmpty()) {
                try {
                    UUID userId = UUID.fromString(userIdStr);
                    authentication = new UserAuthentication(userId, userEmail, authorities);
                    logger.debug("Authentication set with User ID: {}, Email: {}", userId, userEmail);
                } catch (IllegalArgumentException e) {
                    logger.warn("Invalid User ID format: {}, falling back to email-only authentication", userIdStr);
                    authentication = new UserAuthentication(null, userEmail, authorities);
                }
            } else {
                authentication = new UserAuthentication(null, userEmail, authorities);
                logger.debug("Authentication set with Email only: {}", userEmail);
            }
            
            SecurityContextHolder.getContext().setAuthentication(authentication);
            
            logger.debug("Authentication set for user: {} with authorities: {}", 
                userEmail, authorities.stream()
                    .map(GrantedAuthority::getAuthority)
                    .collect(Collectors.toList()));
        } else {
            logger.debug("No authentication headers found, proceeding without authentication");
            SecurityContextHolder.clearContext();
        }
        
        filterChain.doFilter(request, response);
    }
    
    private Collection<GrantedAuthority> parseRoles(String rolesHeader) {
        if (rolesHeader == null || rolesHeader.trim().isEmpty()) {
            return Collections.emptyList();
        }
        
        return Arrays.stream(rolesHeader.split(","))
                .map(String::trim)
                .filter(role -> !role.isEmpty())
                .map(role -> role.startsWith("ROLE_") ? role : "ROLE_" + role)
                .map(SimpleGrantedAuthority::new)
                .collect(Collectors.toList());
    }
}