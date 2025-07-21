package com.example.userservice.controller;

import com.example.userservice.dto.UserDto;
import com.example.userservice.dto.request.CreateUserRequest;
import com.example.userservice.dto.request.UpdateUserRequest;
import com.example.userservice.dto.response.UserProfileResponse;
import com.example.userservice.dto.response.PublicUserResponse;
import com.example.userservice.dto.response.AdminUserResponse;
import com.example.userservice.service.UserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.validation.annotation.Validated;
import javax.validation.Valid;
import javax.servlet.http.HttpServletRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api")
@Validated
@Tag(name = "User Management", description = "CRUD operations for user entities")
public class UserController {
    
    private static final Logger logger = LoggerFactory.getLogger(UserController.class);
    
    @Autowired
    private UserService userService;
    
    @GetMapping("/health")
    @Operation(summary = "Health Check", description = "Check User service health status")
    @ApiResponse(responseCode = "200", description = "Service is healthy")
    public ResponseEntity<Map<String, String>> health(HttpServletRequest request) {
        logger.info("=== HEALTH CHECK REQUEST ====");
        logger.info("Remote IP: {}", request.getRemoteAddr());
        logger.info("User-Agent: {}", request.getHeader("User-Agent"));
        logger.info("X-Forwarded-For: {}", request.getHeader("X-Forwarded-For"));
        logger.info("X-Real-IP: {}", request.getHeader("X-Real-IP"));
        return ResponseEntity.ok(Map.of("status", "OK", "service", "User Service"));
    }
    
    @GetMapping("/users")
    @Operation(summary = "Get All Users", description = "Retrieve a paginated list of all users")
    @ApiResponse(responseCode = "200", description = "Users retrieved successfully")
    public ResponseEntity<List<PublicUserResponse>> getUsers(
            @Parameter(description = "Page number (0-based)") @RequestParam(defaultValue = "0") int page,
            @Parameter(description = "Page size") @RequestParam(defaultValue = "10") int size,
            HttpServletRequest request) {
        logger.info("=== GET USERS REQUEST ====");
        logger.info("Remote IP: {}", request.getRemoteAddr());
        logger.info("Request URI: {}", request.getRequestURI());
        logger.info("Query String: {}", request.getQueryString());
        List<UserDto> users = userService.getAllUsers(page, size);
        List<PublicUserResponse> publicUsers = users.stream()
                .map(user -> new PublicUserResponse(userService.getUserEntityById(user.getId()).get()))
                .collect(Collectors.toList());
        return ResponseEntity.ok(publicUsers);
    }
    
    @GetMapping("/users/{id}")
    @Operation(summary = "Get User by ID", description = "Retrieve a specific user by their ID")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "User found"),
        @ApiResponse(responseCode = "404", description = "User not found")
    })
    public ResponseEntity<UserDto> getUserById(@Parameter(description = "User ID") @PathVariable Long id) {
        Optional<UserDto> user = userService.getUserById(id);
        return user.map(ResponseEntity::ok)
                   .orElse(ResponseEntity.notFound().build());
    }
    
    @GetMapping("/users/email/{email}")
    public ResponseEntity<UserDto> getUserByEmail(@PathVariable String email) {
        Optional<UserDto> user = userService.getUserByEmail(email);
        return user.map(ResponseEntity::ok)
                   .orElse(ResponseEntity.notFound().build());
    }
    
    @PostMapping("/users")
    @Operation(summary = "Create User", description = "Create a new user account")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "User created successfully"),
        @ApiResponse(responseCode = "400", description = "Email already exists or validation error")
    })
    public ResponseEntity<?> createUser(@Valid @RequestBody CreateUserRequest request, HttpServletRequest httpRequest) {
        logger.info("=== CREATE USER REQUEST ====");
        logger.info("Remote IP: {}", httpRequest.getRemoteAddr());
        logger.info("Request Method: {}", httpRequest.getMethod());
        logger.info("Request URI: {}", httpRequest.getRequestURI());
        logger.info("Content-Type: {}", httpRequest.getHeader("Content-Type"));
        logger.info("User-Agent: {}", httpRequest.getHeader("User-Agent"));
        logger.info("X-Forwarded-For: {}", httpRequest.getHeader("X-Forwarded-For"));
        logger.info("Request Body - Email: {}, Name: {}", request.getEmail(), request.getName());
        try {
            UserDto user = userService.createUser(request.getEmail(), request.getName(), request.getPassword());
            UserProfileResponse profile = new UserProfileResponse(userService.getUserEntityById(user.getId()).get());
            return ResponseEntity.ok(profile);
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }
    
    @PutMapping("/users/{id}")
    public ResponseEntity<?> updateUser(@PathVariable Long id, @Valid @RequestBody UpdateUserRequest request) {
        try {
            Optional<UserDto> user = userService.updateUser(id, request.getName(), request.getEmail());
            if (user.isPresent()) {
                UserProfileResponse profile = new UserProfileResponse(userService.getUserEntityById(user.get().getId()).get());
                return ResponseEntity.ok(profile);
            }
            return ResponseEntity.notFound().build();
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }
    
    @DeleteMapping("/users/{id}")
    public ResponseEntity<?> deleteUser(@PathVariable Long id) {
        boolean deleted = userService.deleteUser(id);
        if (deleted) {
            return ResponseEntity.ok(Map.of("message", "User deleted successfully"));
        }
        return ResponseEntity.notFound().build();
    }
    
    @PostMapping("/users/authenticate")
    @Operation(summary = "Authenticate User", description = "Verify user credentials for login")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Authentication successful"),
        @ApiResponse(responseCode = "400", description = "Invalid credentials")
    })
    public ResponseEntity<?> authenticateUser(@RequestBody Map<String, String> credentials) {
        try {
            String email = credentials.get("email");
            String password = credentials.get("password");
            
            Optional<UserDto> user = userService.authenticateUser(email, password);
            if (user.isPresent()) {
                UserProfileResponse profile = new UserProfileResponse(userService.getUserEntityById(user.get().getId()).get());
                return ResponseEntity.ok(profile);
            }
            return ResponseEntity.badRequest().body(Map.of("error", "Invalid credentials"));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }
}