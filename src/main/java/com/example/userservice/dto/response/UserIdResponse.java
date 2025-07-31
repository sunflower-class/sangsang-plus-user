package com.example.userservice.dto.response;

import java.util.UUID;

public class UserIdResponse {
    private UUID userId;
    private String email;
    
    public UserIdResponse() {}
    
    public UserIdResponse(UUID userId, String email) {
        this.userId = userId;
        this.email = email;
    }
    
    public UUID getUserId() {
        return userId;
    }
    
    public void setUserId(UUID userId) {
        this.userId = userId;
    }
    
    public String getEmail() {
        return email;
    }
    
    public void setEmail(String email) {
        this.email = email;
    }
}