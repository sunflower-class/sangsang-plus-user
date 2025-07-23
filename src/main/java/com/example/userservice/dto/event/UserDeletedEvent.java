package com.example.userservice.dto.event;

public class UserDeletedEvent extends BaseUserEvent {
    
    public static final String EVENT_TYPE = "USER_DELETED";

    public UserDeletedEvent() {
        super();
    }

    public UserDeletedEvent(Long userId, String email) {
        super(userId, email);
    }

    @Override
    public String getEventType() {
        return EVENT_TYPE;
    }
}