package com.example.userservice.event.model;

import java.util.UUID;

public class UserSuspendedEvent extends BaseUserEvent {
    
    public static final String EVENT_TYPE = "USER_SUSPENDED";

    public UserSuspendedEvent() {
        super();
    }

    public UserSuspendedEvent(UUID userId, String email) {
        super(userId, email);
    }

    @Override
    public String getEventType() {
        return EVENT_TYPE;
    }
}