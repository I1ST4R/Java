package com.example.javas.controllers;

import com.example.javas.dto.ScheduleIntervalDTO;
import com.example.javas.models.Trainer;
import com.example.javas.models.Users;
import com.example.javas.service.TrainerScheduleService;
import com.example.javas.service.TrainerService;
import com.example.javas.service.UserService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDate;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

@Controller
@RequestMapping("/trainers")
public class TrainerController {
    private static final Logger logger = LoggerFactory.getLogger(TrainerController.class);
    private final TrainerService trainerService;
    private final UserService userService;
    private final PasswordEncoder passwordEncoder;
    private final Path uploadDir;
    private final TrainerScheduleService scheduleService;

    @Autowired
    public TrainerController(TrainerService trainerService, UserService userService, PasswordEncoder passwordEncoder, TrainerScheduleService scheduleService) {
        this.trainerService = trainerService;
        this.userService = userService;
        this.passwordEncoder = passwordEncoder;
        this.scheduleService = scheduleService;
        this.uploadDir = Paths.get("uploads").toAbsolutePath().normalize();
        try {
            Files.createDirectories(uploadDir);
            logger.info("Upload directory created at: {}", uploadDir);
        } catch (IOException e) {
            logger.error("Could not create upload directory", e);
            throw new RuntimeException("Could not create upload directory", e);
        }
    }

    @GetMapping
    public String listTrainers(Model model) {
        List<Trainer> trainers = trainerService.getAllTrainers();
        logger.info("Found {} trainers", trainers.size());
        model.addAttribute("trainers", trainers);
        return "trainer/list";
    }

    @GetMapping("/{id}")
    public String viewTrainer(@PathVariable Long id, Model model, Authentication authentication) {
        Trainer trainer = trainerService.getTrainerById(id);
        if (trainer == null) {
            return "redirect:/trainers";
        }

        LocalDate today = LocalDate.now();
        LocalDate twoWeeksLater = today.plusWeeks(2);
        
        List<com.example.javas.dto.TrainerScheduleDTO> schedules = scheduleService.getTrainerScheduleForPeriod(id, today, twoWeeksLater);
        model.addAttribute("schedules", schedules);
        model.addAttribute("trainer", trainer);

        // Проверяем, является ли текущий пользователь тренером
        boolean isCurrentUserTrainer = false;
        if (authentication != null) {
            for (GrantedAuthority authority : authentication.getAuthorities()) {
                if ("ROLE_TRAINER".equals(authority.getAuthority())) {
                    isCurrentUserTrainer = true;
                    break;
                }
            }
        }
        model.addAttribute("isCurrentUserTrainer", isCurrentUserTrainer);

        return "trainer/details";
    }

    @PostMapping("/add")
    public String addTrainer(
            @RequestParam String username,
            @RequestParam String password,
            @RequestParam String firstName,
            @RequestParam String lastName,
            @RequestParam String specialization,
            @RequestParam String email,
            @RequestParam String phone,
            @RequestParam double hourlyRate,
            @RequestParam int experienceYears,
            @RequestParam(required = false) String description,
            @RequestParam(required = false) MultipartFile photo,
            @RequestParam(required = false) String skills,
            Model model) {

        try {
            logger.info("Starting to create new trainer with username: {}", username);

            // Create user
            Users user = new Users();
            user.setUsername(username);
            user.setPassword(passwordEncoder.encode(password));
            user.setEmail(email);
            logger.info("Creating user: {}", user);
            user = userService.saveUser(user);
            logger.info("User created with ID: {}", user.getId());

            // Assign trainer role
            logger.info("Assigning ROLE_TRAINER to user");
            userService.assignRoleToUser(user.getId(), "ROLE_TRAINER");

            // Create trainer
            Trainer trainer = new Trainer();
            trainer.setFirstName(firstName);
            trainer.setLastName(lastName);
            trainer.setSpecialization(specialization);
            trainer.setEmail(email);
            trainer.setPhone(phone);
            trainer.setHourlyRate(hourlyRate);
            trainer.setExperienceYears(experienceYears);
            trainer.setDescription(description);
            trainer.setUser(user);
            logger.info("Created trainer object: {}", trainer);

            if (photo != null && !photo.isEmpty()) {
                logger.info("Processing photo upload");
                trainer = trainerService.saveTrainerWithPhoto(trainer, photo);
            } else {
                logger.info("Saving trainer without photo");
                trainer = trainerService.saveTrainer(trainer);
            }

            if (skills != null && !skills.isEmpty()) {
                logger.info("Adding skills: {}", skills);
                trainerService.addSkills(trainer.getId(), skills);
            }

            logger.info("Trainer saved successfully with ID: {}", trainer.getId());
            return "redirect:/trainers";

        } catch (RuntimeException e) {
            logger.error("Error creating trainer: {}", e.getMessage(), e);
            String errorMessage = e.getMessage();
            if (errorMessage.contains("username")) {
                model.addAttribute("error", "Пользователь с таким именем уже существует");
            } else if (errorMessage.contains("email")) {
                model.addAttribute("error", "Пользователь с таким email уже существует");
            } else {
                model.addAttribute("error", "Произошла ошибка при создании тренера: " + e.getMessage());
            }
            return "trainer/list";
        } catch (Exception e) {
            logger.error("Unexpected error creating trainer: {}", e.getMessage(), e);
            model.addAttribute("error", "Произошла непредвиденная ошибка при создании тренера");
            return "trainer/list";
        }
    }

    @PostMapping("/{id}/delete")
    public String deleteTrainer(@PathVariable Long id) {
        trainerService.deleteTrainer(id);
        return "redirect:/trainers";
    }
} 