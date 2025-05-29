package com.example.javas.service;

import com.example.javas.models.Training;
import com.example.javas.repository.TrainingRepository;
import com.example.javas.models.Users;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class TrainingService {
    private static final Logger logger = LoggerFactory.getLogger(TrainingService.class);
    
    private final TrainingRepository trainingRepository;
    private final UserService userService;

    public TrainingService(TrainingRepository trainingRepository, UserService userService) {
        this.trainingRepository = trainingRepository;
        this.userService = userService;
    }

    @Transactional(readOnly = true)
    public List<Training> getClientTrainings() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        String username = auth.getName();
        Users client = userService.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("User not found"));
        
        List<Training> trainings = trainingRepository.findByClientIdOrderByDateDescStartTimeDesc(client.getId());
        logger.info("Retrieved {} trainings for client {}", trainings.size(), username);
        return trainings;
    }
} 