package com.example.javas.controller;

import com.example.javas.entity.Trainer;
import com.example.javas.service.TrainerService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;

@Controller
@RequestMapping("/trainers")
public class TrainerController {
    private static final Logger logger = LoggerFactory.getLogger(TrainerController.class);
    private final TrainerService trainerService;
    private final Path uploadDir;

    @Autowired
    public TrainerController(TrainerService trainerService) {
        this.trainerService = trainerService;
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
        return "trainers/list";
    }

    @GetMapping("/{id}")
    public String viewTrainer(@PathVariable Long id, Model model) {
        Trainer trainer = trainerService.getTrainerById(id);
        logger.info("Viewing trainer: {}", trainer);
        model.addAttribute("trainer", trainer);
        return "trainers/details";
    }

    @PostMapping("/add")
    public String addTrainer(
            @RequestParam("firstName") String firstName,
            @RequestParam("lastName") String lastName,
            @RequestParam("specialization") String specialization,
            @RequestParam("email") String email,
            @RequestParam("phone") String phone,
            @RequestParam("hourlyRate") double hourlyRate,
            @RequestParam("experienceYears") int experienceYears,
            @RequestParam("description") String description,
            @RequestParam("skills") String skills,
            @RequestParam("photo") MultipartFile photo) {

        logger.info("Adding new trainer: {} {}", firstName, lastName);

        Trainer trainer = new Trainer();
        trainer.setFirstName(firstName);
        trainer.setLastName(lastName);
        trainer.setSpecialization(specialization);
        trainer.setEmail(email);
        trainer.setPhone(phone);
        trainer.setHourlyRate(hourlyRate);
        trainer.setExperienceYears(experienceYears);
        trainer.setDescription(description);

        // Process skills
        List<String> skillsList = Arrays.asList(skills.split("\\s*,\\s*"));
        trainer.setSkills(skillsList);

        // Handle photo upload
        if (!photo.isEmpty()) {
            try {
                String fileName = UUID.randomUUID().toString() + "_" + photo.getOriginalFilename();
                Path filePath = uploadDir.resolve(fileName);
                Files.copy(photo.getInputStream(), filePath);
                trainer.setPhotoPath(fileName);
                logger.info("Photo uploaded successfully: {}", fileName);
            } catch (IOException e) {
                logger.error("Could not store file", e);
                throw new RuntimeException("Could not store file", e);
            }
        } else {
            trainer.setPhotoPath("default-trainer.jpg");
            logger.info("Using default photo");
        }

        Trainer savedTrainer = trainerService.saveTrainer(trainer);
        logger.info("Trainer saved successfully with ID: {}", savedTrainer.getId());
        return "redirect:/trainers";
    }
} 