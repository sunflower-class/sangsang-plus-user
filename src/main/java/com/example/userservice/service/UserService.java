package com.example.userservice.service;

import com.example.userservice.model.User;
import com.example.userservice.model.Provider;
import com.example.userservice.repository.UserRepository;
import com.example.userservice.dto.UserDto;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class UserService {
    
    @Autowired
    private UserRepository userRepository;
    
    private BCryptPasswordEncoder passwordEncoder = new BCryptPasswordEncoder();
    
    public Optional<User> getUserEntityById(Long id) {
        return userRepository.findById(id);
    }
    
    public List<UserDto> getAllUsers(int page, int size) {
        Pageable pageable = PageRequest.of(page, size);
        Page<User> users = userRepository.findAll(pageable);
        return users.getContent().stream()
                .map(UserDto::new)
                .collect(Collectors.toList());
    }
    
    public Optional<UserDto> getUserById(Long id) {
        return userRepository.findById(id)
                .map(UserDto::new);
    }
    
    public Optional<UserDto> getUserByEmail(String email) {
        return userRepository.findByEmail(email)
                .map(UserDto::new);
    }
    
    public UserDto createUser(String email, String name, String password) {
        if (userRepository.existsByEmail(email)) {
            throw new RuntimeException("Email already exists");
        }
        
        User user = new User(email, name, passwordEncoder.encode(password));
        User saved = userRepository.save(user);
        return new UserDto(saved);
    }
    
    public Optional<UserDto> updateUser(Long id, String name, String email) {
        Optional<User> userOpt = userRepository.findById(id);
        if (userOpt.isPresent()) {
            User user = userOpt.get();
            if (name != null) user.setName(name);
            if (email != null && !email.equals(user.getEmail())) {
                if (userRepository.existsByEmail(email)) {
                    throw new RuntimeException("Email already exists");
                }
                user.setEmail(email);
            }
            User saved = userRepository.save(user);
            return Optional.of(new UserDto(saved));
        }
        return Optional.empty();
    }
    
    public boolean deleteUser(Long id) {
        if (userRepository.existsById(id)) {
            userRepository.deleteById(id);
            return true;
        }
        return false;
    }
    
    public UserDto createOAuth2User(String email, String name, Provider provider) {
        if (userRepository.existsByEmail(email)) {
            throw new RuntimeException("Email already exists");
        }
        
        User user = new User(email, name, null); // OAuth2 users don't have password
        user.setProvider(provider);
        user.setEmailVerified(true); // OAuth2 users are email verified
        User saved = userRepository.save(user);
        return new UserDto(saved);
    }
    
    public void updateLoginInfo(Long userId) {
        Optional<User> userOpt = userRepository.findById(userId);
        if (userOpt.isPresent()) {
            User user = userOpt.get();
            user.setLastLoginAt(LocalDateTime.now());
            user.setLoginCount(user.getLoginCount() + 1);
            userRepository.save(user);
        }
    }
    
    public Optional<UserDto> authenticateUser(String email, String password) {
        Optional<User> userOpt = userRepository.findByEmail(email);
        if (userOpt.isPresent()) {
            User user = userOpt.get();
            if (passwordEncoder.matches(password, user.getPassword())) {
                // Update login info
                user.setLastLoginAt(LocalDateTime.now());
                user.setLoginCount(user.getLoginCount() + 1);
                userRepository.save(user);
                return Optional.of(new UserDto(user));
            }
        }
        return Optional.empty();
    }
}