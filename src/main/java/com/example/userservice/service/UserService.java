package com.example.userservice.service;

import com.example.userservice.model.User;
import com.example.userservice.repository.UserRepository;
import com.example.userservice.dto.UserDto;
import com.example.userservice.event.publisher.UserEventProducer;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
public class UserService {
    
    @Autowired
    private UserRepository userRepository;

    @Autowired
    private UserEventProducer userEventProducer;
    
    public Optional<User> getUserEntityById(UUID id) {
        return userRepository.findById(id);
    }
    
    public List<UserDto> getAllUsers(int page, int size) {
        Pageable pageable = PageRequest.of(page, size);
        Page<User> users = userRepository.findAll(pageable);
        return users.getContent().stream()
                .map(UserDto::new)
                .collect(Collectors.toList());
    }
    
    public Optional<UserDto> getUserById(UUID id) {
        return userRepository.findById(id)
                .map(UserDto::new);
    }
    
    public Optional<UserDto> getUserByEmail(String email) {
        return userRepository.findByEmail(email)
                .map(UserDto::new);
    }
    
    public UserDto createUser(String email, String name) {
        if (userRepository.existsByEmail(email)) {
            throw new RuntimeException("Email already exists");
        }
        
        User user = new User(email, name);
        User saved = userRepository.save(user);
        
        // Publish user created event for Product server
        userEventProducer.publishUserCreatedEvent(saved);
        
        return new UserDto(saved);
    }
    
    public Optional<UserDto> updateUser(UUID id, String name) {
        Optional<User> userOpt = userRepository.findById(id);
        if (userOpt.isPresent()) {
            User user = userOpt.get();
            if (name != null) {
                user.setName(name);
            }
            User saved = userRepository.save(user);
            
            // Publish user updated event
            userEventProducer.publishUserUpdatedEvent(saved);
            
            return Optional.of(new UserDto(saved));
        }
        return Optional.empty();
    }
    
    public boolean deleteUser(UUID id) {
        Optional<User> userOpt = userRepository.findById(id);
        if (userOpt.isPresent()) {
            User user = userOpt.get();
            
            // Publish user deleted event before deletion
            userEventProducer.publishUserDeletedEvent(user);
            
            userRepository.deleteById(id);
            return true;
        }
        return false;
    }
    
}