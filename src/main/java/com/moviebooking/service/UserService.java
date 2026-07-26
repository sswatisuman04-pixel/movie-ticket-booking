package com.moviebooking.service;

import com.moviebooking.entity.User;
import com.moviebooking.enums.Role;
import com.moviebooking.exception.DuplicateResourceException;
import com.moviebooking.exception.ResourceNotFoundException;
import com.moviebooking.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@RequiredArgsConstructor
public class UserService {

    private final UserRepository userRepository;

    @Transactional
    public User createUser(String name, String email, String password, Role role) {
        if (userRepository.existsByEmail(email)) {
            throw new DuplicateResourceException("User with email '" + email + "' already exists");
        }
        User user = User.builder()
                .name(name)
                .email(email)
                .password(password)
                .role(role)
                .build();
        log.info("Creating user with email: {}", email);
        return userRepository.save(user);
    }

    public User findByEmail(String email) {
        return userRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with email: " + email));
    }

    public boolean existsByEmail(String email) {
        return userRepository.existsByEmail(email);
    }
}
