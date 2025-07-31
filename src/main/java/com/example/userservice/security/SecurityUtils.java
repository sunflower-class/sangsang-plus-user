package com.example.userservice.security;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Collection;
import java.util.Optional;
import java.util.UUID;

public class SecurityUtils {
    
    private static final Logger logger = LoggerFactory.getLogger(SecurityUtils.class);
    
    public static Optional<UUID> getCurrentUserId() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication instanceof UserAuthentication) {
            UserAuthentication userAuth = (UserAuthentication) authentication;
            return Optional.ofNullable(userAuth.getUserId());
        }
        return Optional.empty();
    }
    
    public static Optional<String> getCurrentUserEmail() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication instanceof UserAuthentication) {
            UserAuthentication userAuth = (UserAuthentication) authentication;
            return Optional.of(userAuth.getEmail());
        } else if (authentication != null && authentication.isAuthenticated() 
            && authentication.getPrincipal() instanceof String) {
            return Optional.of((String) authentication.getPrincipal());
        }
        return Optional.empty();
    }
    
    public static Collection<? extends GrantedAuthority> getCurrentUserAuthorities() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication != null && authentication.isAuthenticated()) {
            return authentication.getAuthorities();
        }
        return java.util.Collections.emptyList();
    }
    
    public static boolean hasRole(String role) {
        Collection<? extends GrantedAuthority> authorities = getCurrentUserAuthorities();
        String roleWithPrefix = role.startsWith("ROLE_") ? role : "ROLE_" + role;
        
        boolean hasRole = authorities.stream()
            .anyMatch(authority -> authority.getAuthority().equals(roleWithPrefix));
        
        logger.debug("Checking role '{}' for user: {} - Result: {}", 
            roleWithPrefix, getCurrentUserEmail().orElse("anonymous"), hasRole);
        
        return hasRole;
    }
    
    public static boolean isCurrentUser(String email) {
        Optional<String> currentEmail = getCurrentUserEmail();
        boolean isCurrentUser = currentEmail.isPresent() && currentEmail.get().equals(email);
        
        logger.debug("Checking if current user '{}' matches email '{}' - Result: {}", 
            currentEmail.orElse("anonymous"), email, isCurrentUser);
        
        return isCurrentUser;
    }
    
    public static boolean canAccessUser(UUID targetUserId) {
        if (hasRole("ADMIN")) {
            logger.debug("User has ADMIN role, access granted to user ID: {}", targetUserId);
            return true;
        }
        
        Optional<UUID> currentUserId = getCurrentUserId();
        if (currentUserId.isPresent() && targetUserId != null) {
            boolean canAccess = currentUserId.get().equals(targetUserId);
            logger.debug("User ID comparison check: current={}, target={} - Result: {}", 
                currentUserId.get(), targetUserId, canAccess);
            return canAccess;
        }
        
        logger.debug("No current user ID found or target user ID is null - Access denied");
        return false;
    }
    
    // For email-based endpoints (backward compatibility)
    public static boolean canAccessUserByEmail(String targetUserEmail) {
        if (hasRole("ADMIN")) {
            logger.debug("User has ADMIN role, access granted to email user: {}", targetUserEmail);
            return true;
        }
        
        return isCurrentUser(targetUserEmail);
    }
}