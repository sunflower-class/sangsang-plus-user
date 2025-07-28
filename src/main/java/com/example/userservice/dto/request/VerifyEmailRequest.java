package com.example.userservice.dto.request;

import javax.validation.constraints.Email;
import javax.validation.constraints.NotBlank;

public class VerifyEmailRequest {
    @NotBlank(message = "Email is required")
    @Email(message = "Email should be valid")
    private String email;
    
    public String getEmail() {
        return email;
    }
    
    public void setEmail(String email) {
        this.email = email;
    }
}