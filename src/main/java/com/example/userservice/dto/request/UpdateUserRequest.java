package com.example.userservice.dto.request;

import javax.validation.constraints.Size;
import javax.validation.constraints.Pattern;

public class UpdateUserRequest {
    @Size(min = 2, max = 100, message = "Name must be between 2 and 100 characters")
    private String name;
    
    @Pattern(regexp = "^(?=.*[0-9])(?=.*[a-z])(?=.*[A-Z])(?=.*[!@#$%^&+=])(?=\\S+$).{8,}",
             message = "비밀번호는 8자 이상이어야 하며, 숫자, 소문자, 대문자, 특수문자를 각각 하나 이상 포함해야 합니다.")
    private String password;
    
    public String getName() { 
        return name; 
    }
    
    public void setName(String name) { 
        this.name = name; 
    }
    
    public String getPassword() { 
        return password; 
    }
    
    public void setPassword(String password) { 
        this.password = password; 
    }
}