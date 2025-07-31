package com.example.userservice.event.model;

import java.util.UUID;

public class UserDeletedEvent extends BaseUserEvent {
    
    public static final String EVENT_TYPE = "USER_DELETED";

    public UserDeletedEvent() {
        super();
    }

    public UserDeletedEvent(UUID userId, String email) {
        super(userId, email);
    }

    @Override
    public String getEventType() {
        return EVENT_TYPE;
    }
}