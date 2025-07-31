package com.example.userservice.event.model;

import java.util.UUID;

/**
 * User profile event for Product server consumption
 * Contains basic user information needed for product ownership tracking
 */
public class UserProfileEvent extends BaseUserEvent {
    
    public static final String EVENT_TYPE = "USER_PROFILE";
    
    private String name;
    private String action; // CREATED, UPDATED, DELETED

    public UserProfileEvent() {
        super();
    }

    public UserProfileEvent(UUID userId, String email, String name, String action) {
        super(userId, email);
        this.name = name;
        this.action = action;
    }

    @Override
    public String getEventType() {
        return EVENT_TYPE;
    }

    // Getters and Setters
    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getAction() {
        return action;
    }

    public void setAction(String action) {
        this.action = action;
    }
}