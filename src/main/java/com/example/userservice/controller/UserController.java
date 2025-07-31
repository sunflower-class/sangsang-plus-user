package com.example.userservice.controller;

import com.example.userservice.dto.UserDto;
import com.example.userservice.dto.request.CreateUserRequest;
import com.example.userservice.dto.request.UpdateUserRequest;
import com.example.userservice.dto.response.UserProfileResponse;
import com.example.userservice.dto.response.PublicUserResponse;
import com.example.userservice.dto.response.UserIdResponse;
import com.example.userservice.service.UserService;
import com.example.userservice.event.publisher.UserEventProducer;
import com.example.userservice.model.User;
import com.example.userservice.security.SecurityUtils;
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
import java.util.UUID;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/users")
@Validated
@Tag(name = "User Management", description = "CRUD operations for user entities")
public class UserController {
    
    private static final Logger logger = LoggerFactory.getLogger(UserController.class);
    
    @Autowired
    private UserService userService;
    
    @Autowired
    private UserEventProducer userEventProducer;
    
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
    
    @PostMapping("/test-event")
    @Operation(summary = "Test Event Publishing", description = "Test endpoint for publishing user events")
    @ApiResponse(responseCode = "200", description = "Event published successfully")
    public ResponseEntity<Map<String, String>> testEvent(@RequestBody Map<String, String> request) {
        try {
            String eventType = request.getOrDefault("eventType", "USER_UPDATED");
            UUID userId = UUID.fromString(request.getOrDefault("userId", UUID.randomUUID().toString()));
            
            // Create a dummy user for testing
            User testUser = new User();
            testUser.setId(userId);
            testUser.setEmail(request.getOrDefault("email", "test@example.com"));
            testUser.setName(request.getOrDefault("name", "Test User"));
            
            // Publish event based on type
            switch (eventType) {
                case "USER_DELETED":
                    userEventProducer.publishUserDeletedEvent(testUser);
                    break;
                case "USER_SUSPENDED":
                    userEventProducer.publishUserSuspendedEvent(testUser);
                    break;
                case "USER_UPDATED":
                default:
                    userEventProducer.publishUserUpdatedEvent(testUser);
                    break;
            }
            
            logger.info("Test event published: {} for user ID: {}", eventType, userId);
            return ResponseEntity.ok(Map.of(
                "status", "SUCCESS", 
                "eventType", eventType,
                "userId", userId.toString()
            ));
        } catch (Exception e) {
            logger.error("Error publishing test event", e);
            return ResponseEntity.ok(Map.of("status", "ERROR", "message", e.getMessage()));
        }
    }
    
    @GetMapping("")
    @Operation(summary = "Get All Users", description = "Retrieve a paginated list of all users")
    @ApiResponse(responseCode = "200", description = "Users retrieved successfully")
    public ResponseEntity<?> getUsers(
            @Parameter(description = "Page number (0-based)") @RequestParam(defaultValue = "0") int page,
            @Parameter(description = "Page size") @RequestParam(defaultValue = "10") int size,
            HttpServletRequest request) {
        logger.info("=== GET USERS REQUEST ====");
        logger.info("Current user: {}", SecurityUtils.getCurrentUserEmail().orElse("anonymous"));
        logger.info("Remote IP: {}", request.getRemoteAddr());
        logger.info("Request URI: {}", request.getRequestURI());
        logger.info("Query String: {}", request.getQueryString());
        
        if (!SecurityUtils.hasRole("ADMIN")) {
            logger.warn("Unauthorized access attempt to get all users by: {}", 
                SecurityUtils.getCurrentUserEmail().orElse("anonymous"));
            return ResponseEntity.status(403).body(Map.of("error", "Access denied. Admin role required."));
        }
        
        List<UserDto> users = userService.getAllUsers(page, size);
        List<PublicUserResponse> publicUsers = users.stream()
                .map(user -> userService.getUserEntityById(user.getId())
                        .map(PublicUserResponse::new)
                        .orElse(null))
                .filter(response -> response != null)
                .collect(Collectors.toList());
        return ResponseEntity.ok(publicUsers);
    }
    
    @GetMapping("/{id}")
    @Operation(summary = "Get User by ID", description = "Retrieve a specific user by their ID")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "User found"),
        @ApiResponse(responseCode = "403", description = "Access denied"),
        @ApiResponse(responseCode = "404", description = "User not found")
    })
    public ResponseEntity<?> getUserById(@Parameter(description = "User ID") @PathVariable UUID id) {
        if (!SecurityUtils.canAccessUser(id)) {
            logger.warn("Unauthorized access attempt to user ID {} by: {}", 
                id, SecurityUtils.getCurrentUserEmail().orElse("anonymous"));
            return ResponseEntity.status(403).body(Map.of("error", "Access denied. You can only access your own profile or need admin role."));
        }
        
        Optional<UserDto> user = userService.getUserById(id);
        return user.map(ResponseEntity::ok)
                   .orElse(ResponseEntity.notFound().build());
    }
    
    @GetMapping("/me")
    @Operation(summary = "Get Current User Profile", description = "Retrieve the current authenticated user's profile")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "User profile retrieved successfully"),
        @ApiResponse(responseCode = "401", description = "User not authenticated"),
        @ApiResponse(responseCode = "404", description = "User not found")
    })
    public ResponseEntity<?> getCurrentUserProfile() {
        Optional<String> currentEmail = SecurityUtils.getCurrentUserEmail();
        if (!currentEmail.isPresent()) {
            return ResponseEntity.status(401).body(Map.of("error", "User not authenticated"));
        }
        
        logger.info("Current user profile request from: {}", currentEmail.get());
        
        Optional<UserDto> user = userService.getUserByEmail(currentEmail.get());
        if (user.isPresent()) {
            Optional<User> userEntity = userService.getUserEntityById(user.get().getId());
            if (userEntity.isPresent()) {
                UserProfileResponse profile = new UserProfileResponse(userEntity.get());
                return ResponseEntity.ok(profile);
            }
        }
        
        return ResponseEntity.notFound().build();
    }

    @GetMapping("/email/{email}")
    public ResponseEntity<?> getUserByEmail(@PathVariable String email) {
        if (!SecurityUtils.canAccessUserByEmail(email)) {
            logger.warn("Unauthorized access attempt to user email {} by: {}", 
                email, SecurityUtils.getCurrentUserEmail().orElse("anonymous"));
            return ResponseEntity.status(403).body(Map.of("error", "Access denied. You can only access your own profile or need admin role."));
        }
        
        Optional<UserDto> user = userService.getUserByEmail(email);
        return user.map(ResponseEntity::ok)
                   .orElse(ResponseEntity.notFound().build());
    }
    
    @GetMapping("/gateway/lookup/{email}")
    @Operation(summary = "Gateway User ID Lookup", description = "Internal endpoint for gateway to retrieve user ID by email")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "User ID found"),
        @ApiResponse(responseCode = "404", description = "User not found")
    })
    public ResponseEntity<UserIdResponse> getUserIdByEmail(@PathVariable String email) {
        logger.info("Gateway lookup request for email: {}", email);
        
        Optional<UserDto> user = userService.getUserByEmail(email);
        if (user.isPresent()) {
            UserIdResponse response = new UserIdResponse(user.get().getId(), user.get().getEmail());
            return ResponseEntity.ok(response);
        }
        
        logger.warn("User not found for gateway lookup: {}", email);
        return ResponseEntity.notFound().build();
    }
    
    @PostMapping("")
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
            UserDto user = userService.createUser(request.getEmail(), request.getName());
            Optional<User> userEntity = userService.getUserEntityById(user.getId());
            if (userEntity.isPresent()) {
                UserProfileResponse profile = new UserProfileResponse(userEntity.get());
                return ResponseEntity.ok(profile);
            }
            return ResponseEntity.badRequest().body(Map.of("error", "Failed to retrieve created user"));
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }
    
    @PutMapping("/{id}")
    public ResponseEntity<?> updateUser(@PathVariable UUID id, @Valid @RequestBody UpdateUserRequest request) {
        try {
            if (!SecurityUtils.canAccessUser(id)) {
                logger.warn("Unauthorized update attempt for user ID {} by: {}", 
                    id, SecurityUtils.getCurrentUserEmail().orElse("anonymous"));
                return ResponseEntity.status(403).body(Map.of("error", "Access denied. You can only update your own profile or need admin role."));
            }
            
            Optional<UserDto> user = userService.updateUser(id, request.getName());
            if (user.isPresent()) {
                Optional<User> userEntity = userService.getUserEntityById(user.get().getId());
                if (userEntity.isPresent()) {
                    UserProfileResponse profile = new UserProfileResponse(userEntity.get());
                    return ResponseEntity.ok(profile);
                }
            }
            return ResponseEntity.notFound().build();
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }
    
    @DeleteMapping("/{id}")
    public ResponseEntity<?> deleteUser(@PathVariable UUID id) {
        if (!SecurityUtils.canAccessUser(id)) {
            logger.warn("Unauthorized delete attempt for user ID {} by: {}", 
                id, SecurityUtils.getCurrentUserEmail().orElse("anonymous"));
            return ResponseEntity.status(403).body(Map.of("error", "Access denied. You can only delete your own profile or need admin role."));
        }
        
        boolean deleted = userService.deleteUser(id);
        if (deleted) {
            return ResponseEntity.ok(Map.of("message", "User deleted successfully"));
        }
        return ResponseEntity.notFound().build();
    }
    
}