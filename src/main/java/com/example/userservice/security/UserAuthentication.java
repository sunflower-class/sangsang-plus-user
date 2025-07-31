package com.example.userservice.security;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;

import java.util.Collection;
import java.util.UUID;

public class UserAuthentication implements Authentication {
    
    private final UUID userId;
    private final String email;
    private final Collection<? extends GrantedAuthority> authorities;
    private boolean authenticated = true;

    public UserAuthentication(UUID userId, String email, Collection<? extends GrantedAuthority> authorities) {
        this.userId = userId;
        this.email = email;
        this.authorities = authorities;
    }

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return authorities;
    }

    @Override
    public Object getCredentials() {
        return null; // No credentials needed for header-based auth
    }

    @Override
    public Object getDetails() {
        return null;
    }

    @Override
    public Object getPrincipal() {
        return email; // Keep email as principal for backward compatibility
    }

    @Override
    public boolean isAuthenticated() {
        return authenticated;
    }

    @Override
    public void setAuthenticated(boolean isAuthenticated) throws IllegalArgumentException {
        this.authenticated = isAuthenticated;
    }

    @Override
    public String getName() {
        return email;
    }

    // Custom methods to access user information
    public UUID getUserId() {
        return userId;
    }

    public String getEmail() {
        return email;
    }
}