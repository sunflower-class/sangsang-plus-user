package com.example.userservice.dto.request;

import com.example.userservice.model.Provider;
import javax.validation.constraints.Email;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;

public class CreateOAuth2UserRequest {
    @NotBlank(message = "Email is required")
    @Email(message = "Email should be valid")
    private String email;
    
    @NotBlank(message = "Name is required")
    private String name;
    
    @NotNull(message = "Provider is required")
    private Provider provider;
    
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
    
    public Provider getProvider() {
        return provider;
    }
    
    public void setProvider(Provider provider) {
        this.provider = provider;
    }
}