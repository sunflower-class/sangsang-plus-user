package com.example.userservice.event.model;

import java.util.UUID;

public class UserCreatedEvent extends BaseUserEvent {
    
    public static final String EVENT_TYPE = "USER_CREATED";
    
    private String name;

    public UserCreatedEvent() {
        super();
    }

    public UserCreatedEvent(UUID userId, String email, String name) {
        super(userId, email);
        this.name = name;
    }

    @Override
    public String getEventType() {
        return EVENT_TYPE;
    }

    // Getter and Setter for name
    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }
}