package com.example.javas.service;

import com.example.javas.dto.UserRegistrationDto;
import com.example.javas.repository.RoleRepository;
import com.example.javas.repository.UserRepository;
import com.example.javas.models.ERole;
import com.example.javas.models.Role;
import com.example.javas.models.Users;
import com.example.javas.exception.UsernameAlreadyExistsException;
import com.example.javas.exception.EmailAlreadyExistsException;
import com.example.javas.exception.RoleNotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Collections;
import java.util.Optional;

@Service
@RequiredArgsConstructor
@Slf4j
public class UserService {

    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final PasswordEncoder passwordEncoder;

    @Transactional
    public void registerNewUser(UserRegistrationDto registrationDto) {
        log.info("Starting user registration process for username: {}", registrationDto.getUsername());
        
        try {
            // Check if username exists
            boolean usernameExists = userRepository.existsByUsername(registrationDto.getUsername());
            log.debug("Username exists check result: {}", usernameExists);
            
            if (usernameExists) {
                log.warn("Username already exists: {}", registrationDto.getUsername());
                throw new UsernameAlreadyExistsException("Username is already taken");
            }

            // Check if email exists
            boolean emailExists = userRepository.existsByEmail(registrationDto.getEmail());
            log.debug("Email exists check result: {}", emailExists);
            
            if (emailExists) {
                log.warn("Email already exists: {}", registrationDto.getEmail());
                throw new EmailAlreadyExistsException("Email is already in use");
            }

            // Create new user
            Users user = new Users();
            user.setUsername(registrationDto.getUsername());
            user.setEmail(registrationDto.getEmail());
            user.setPassword(passwordEncoder.encode(registrationDto.getPassword()));
            user.setTrainerId(null);

            log.debug("Created user object: {}", user);

            // Get user role
            Role userRole = roleRepository.findByName(ERole.ROLE_USER)
                    .orElseThrow(() -> {
                        log.error("Role ROLE_USER not found in database");
                        return new RoleNotFoundException("Role not found");
                    });
            log.debug("Found user role: {}", userRole);

            user.setRoles(Collections.singleton(userRole));
            log.debug("Set roles for user: {}", user.getRoles());

            // Save user
            try {
                Users savedUser = userRepository.save(user);
                log.info("User successfully saved to database with ID: {}", savedUser.getId());
                
                // Verify the user was saved
                Users verifiedUser = userRepository.findById(savedUser.getId())
                        .orElseThrow(() -> new RuntimeException("User not found after save"));
                log.info("Verified saved user: {}", verifiedUser);
            } catch (Exception e) {
                log.error("Error saving user to database: {}", e.getMessage(), e);
                throw e;
            }
        } catch (Exception e) {
            log.error("Registration failed: {}", e.getMessage(), e);
            throw e;
        }
    }

    @Transactional
    public Users saveUser(Users user) {
        // Проверяем, существует ли пользователь с таким email, только если email изменился
        Users existingUser = userRepository.findById(user.getId()).orElse(null);
        if (existingUser != null && !existingUser.getEmail().equals(user.getEmail())) {
            if (userRepository.existsByEmail(user.getEmail())) {
                throw new RuntimeException("User with email " + user.getEmail() + " already exists");
            }
        }
        return userRepository.save(user);
    }

    @Transactional
    public void assignRoleToUser(Long userId, String roleName) {
        userRepository.assignRoleToUser(userId, roleName);
    }

    public Optional<Users> findByUsername(String username) {
        return userRepository.findByUsername(username);
    }

    public boolean existsByEmail(String email) {
        return userRepository.existsByEmail(email);
    }
} 