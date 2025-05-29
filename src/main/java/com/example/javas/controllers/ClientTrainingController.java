package com.example.javas.controllers;

import com.example.javas.service.TrainingService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
@RequestMapping("/client/trainings")
@PreAuthorize("hasRole('USER')")
public class ClientTrainingController {
    private static final Logger logger = LoggerFactory.getLogger(ClientTrainingController.class);

    private final TrainingService trainingService;

    public ClientTrainingController(TrainingService trainingService) {
        this.trainingService = trainingService;
    }

    @GetMapping
    public String getMyTrainings(Model model) {
        try {
            logger.info("Getting trainings page for user");
            var trainings = trainingService.getClientTrainings();
            logger.info("Retrieved {} trainings", trainings.size());
            
            model.addAttribute("trainings", trainings);
            logger.info("Added trainings to model");
            
            return "client/my-trainings";
        } catch (Exception e) {
            logger.error("Error in getMyTrainings: {}", e.getMessage(), e);
            model.addAttribute("error", "Произошла ошибка при загрузке тренировок: " + e.getMessage());
            return "error";
        }
    }
} 