package com.example.userservice.dto.response;

import com.example.userservice.model.User;

public class PublicUserResponse {
    private Long id;
    private String name;
    private String role;
    
    public PublicUserResponse() {}
    
    public PublicUserResponse(User user) {
        this.id = user.getId();
        this.name = user.getName();
        this.role = user.getRole().toString();
    }
    
    public Long getId() { 
        return id; 
    }
    
    public void setId(Long id) { 
        this.id = id; 
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
}