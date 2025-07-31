package com.example.userservice.dto.response;

import com.example.userservice.model.User;
import java.util.UUID;

public class UserProfileResponse {
    private UUID id;
    private String email;
    private String name;
    
    public UserProfileResponse() {}
    
    public UserProfileResponse(User user) {
        this.id = user.getId();
        this.email = user.getEmail();
        this.name = user.getName();
    }
    
    public UUID getId() { 
        return id; 
    }
    
    public void setId(UUID id) { 
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
}