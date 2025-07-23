package com.example.userservice.dto.event;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonTypeInfo;

import java.time.LocalDateTime;
import java.util.UUID;

@JsonTypeInfo(use = JsonTypeInfo.Id.NAME, property = "eventType")
@JsonSubTypes({
    @JsonSubTypes.Type(value = UserDeletedEvent.class, name = "USER_DELETED"),
    @JsonSubTypes.Type(value = UserSuspendedEvent.class, name = "USER_SUSPENDED"),
    @JsonSubTypes.Type(value = UserUpdatedEvent.class, name = "USER_UPDATED")
})
public abstract class BaseUserEvent {
    
    private String eventId;
    
    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
    private LocalDateTime timestamp;
    
    private Long userId;
    private String email;

    public BaseUserEvent() {
        this.eventId = UUID.randomUUID().toString();
        this.timestamp = LocalDateTime.now();
    }

    public BaseUserEvent(Long userId, String email) {
        this();
        this.userId = userId;
        this.email = email;
    }

    public abstract String getEventType();

    // Getters and Setters
    public String getEventId() {
        return eventId;
    }

    public void setEventId(String eventId) {
        this.eventId = eventId;
    }

    public LocalDateTime getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(LocalDateTime timestamp) {
        this.timestamp = timestamp;
    }

    public Long getUserId() {
        return userId;
    }

    public void setUserId(Long userId) {
        this.userId = userId;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }
}