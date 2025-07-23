package com.example.userservice.dto.event;

public class UserSuspendedEvent extends BaseUserEvent {
    
    public static final String EVENT_TYPE = "USER_SUSPENDED";

    public UserSuspendedEvent() {
        super();
    }

    public UserSuspendedEvent(Long userId, String email) {
        super(userId, email);
    }

    @Override
    public String getEventType() {
        return EVENT_TYPE;
    }
}