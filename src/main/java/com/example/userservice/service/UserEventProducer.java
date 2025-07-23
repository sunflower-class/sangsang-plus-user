package com.example.userservice.service;

import com.example.userservice.dto.event.BaseUserEvent;
import com.example.userservice.dto.event.UserDeletedEvent;
import com.example.userservice.dto.event.UserSuspendedEvent;
import com.example.userservice.dto.event.UserUpdatedEvent;
import com.example.userservice.model.User;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.support.SendResult;
import org.springframework.stereotype.Service;
import org.springframework.util.concurrent.ListenableFuture;
import org.springframework.util.concurrent.ListenableFutureCallback;

@Service
public class UserEventProducer {

    private static final Logger logger = LoggerFactory.getLogger(UserEventProducer.class);
    private static final String TOPIC = "user-events";

    @Autowired
    private KafkaTemplate<String, BaseUserEvent> kafkaTemplate;

    public void publishUserDeletedEvent(User user) {
        UserDeletedEvent event = new UserDeletedEvent(user.getId(), user.getEmail());
        publishEvent(event, "USER_DELETED");
    }

    public void publishUserSuspendedEvent(User user) {
        UserSuspendedEvent event = new UserSuspendedEvent(user.getId(), user.getEmail());
        publishEvent(event, "USER_SUSPENDED");
    }

    public void publishUserUpdatedEvent(User user) {
        UserUpdatedEvent event = new UserUpdatedEvent(user.getId(), user.getEmail(), user.getName());
        publishEvent(event, "USER_UPDATED");
    }

    private void publishEvent(BaseUserEvent event, String eventType) {
        try {
            logger.info("Publishing {} event for user ID: {}, email: {}", 
                eventType, event.getUserId(), event.getEmail());

            ListenableFuture<SendResult<String, BaseUserEvent>> future = 
                kafkaTemplate.send(TOPIC, event.getUserId().toString(), event);

            future.addCallback(new ListenableFutureCallback<SendResult<String, BaseUserEvent>>() {
                @Override
                public void onSuccess(SendResult<String, BaseUserEvent> result) {
                    logger.info("Successfully published {} event for user ID: {} with offset: {}", 
                        eventType, event.getUserId(), result.getRecordMetadata().offset());
                }

                @Override
                public void onFailure(Throwable ex) {
                    logger.error("Failed to publish {} event for user ID: {}", 
                        eventType, event.getUserId(), ex);
                }
            });

        } catch (Exception e) {
            logger.error("Error publishing {} event for user ID: {}", 
                eventType, event.getUserId(), e);
        }
    }
}