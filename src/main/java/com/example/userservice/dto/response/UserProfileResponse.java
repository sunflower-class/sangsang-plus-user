package com.example.userservice.dto.response;

import com.example.userservice.model.User;
import java.time.LocalDateTime;

public class UserProfileResponse {
    private Long id;
    private String email;
    private String name;
    private String role;
    private Boolean emailVerified;
    private LocalDateTime createdAt;
    
    public UserProfileResponse() {}
    
    public UserProfileResponse(User user) {
        this.id = user.getId();
        this.email = user.getEmail();
        this.name = user.getName();
        this.role = user.getRole().toString();
        this.emailVerified = user.getEmailVerified();
        this.createdAt = user.getCreatedAt();
    }
    
    public Long getId() { 
        return id; 
    }
    
    public void setId(Long id) { 
        this.id = id; 
    }
    
    public String getEmail() { 
        return email; 
    }
    
    public void setEmail(String email) { 
        this.email = email; 
    }
    
    public String getName() { 
        return name; 
    }
    
    public void setName(String name) { 
        this.name = name; 
    }
    
    public String getRole() { 
        return role; 
    }
    
    public void setRole(String role) { 
        this.role = role; 
    }
    
    public Boolean getEmailVerified() { 
        return emailVerified; 
    }
    
    public void setEmailVerified(Boolean emailVerified) { 
        this.emailVerified = emailVerified; 
    }
    
    public LocalDateTime getCreatedAt() { 
        return createdAt; 
    }
    
    public void setCreatedAt(LocalDateTime createdAt) { 
        this.createdAt = createdAt; 
    }
}