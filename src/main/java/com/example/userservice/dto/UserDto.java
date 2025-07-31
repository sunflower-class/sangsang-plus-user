package com.example.userservice.dto;

import com.example.userservice.model.User;
import java.util.UUID;

public class UserDto {
    private UUID id;
    private String email;
    private String name;
    
    // Constructors
    public UserDto() {}
    
    public UserDto(User user) {
        this.id = user.getId();
        this.email = user.getEmail();
        this.name = user.getName();
    }
    
    public UserDto(UUID id, String email, String name) {
        this.id = id;
        this.email = email;
        this.name = name;
    }
    
    // Getters and Setters
    public UUID getId() { return id; }
    public void setId(UUID id) { this.id = id; }
    
    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }
    
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
}