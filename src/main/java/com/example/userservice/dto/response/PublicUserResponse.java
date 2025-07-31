package com.example.userservice.dto.response;

import com.example.userservice.model.User;
import java.util.UUID;

public class PublicUserResponse {
    private UUID id;
    private String name;
    
    public PublicUserResponse() {}
    
    public PublicUserResponse(User user) {
        this.id = user.getId();
        this.name = user.getName();
    }
    
    public UUID getId() { 
        return id; 
    }
    
    public void setId(UUID id) { 
        this.id = id; 
    }
    
    public String getName() { 
        return name; 
    }
    
    public void setName(String name) { 
        this.name = name; 
    }
}