package com.example.javas.service;

import com.example.javas.models.Trainer;
import com.example.javas.repository.TrainerRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;

@Service
public class TrainerService {
    private static final Logger logger = LoggerFactory.getLogger(TrainerService.class);
    private final TrainerRepository trainerRepository;
    private final Path uploadDir = Paths.get("uploads");

    @Autowired
    public TrainerService(TrainerRepository trainerRepository) {
        this.trainerRepository = trainerRepository;
        logger.info("TrainerService initialized with repository: {}", trainerRepository);
        try {
            Files.createDirectories(uploadDir);
        } catch (IOException e) {
            logger.error("Could not create upload directory", e);
        }
    }

    public List<Trainer> getAllTrainers() {
        List<Trainer> trainers = trainerRepository.findAll();
        logger.info("Retrieved {} trainers from database", trainers.size());
        return trainers;
    }

    public Trainer getTrainerById(Long id) {
        logger.info("Fetching trainer with ID: {}", id);
        return trainerRepository.findById(id)
                .orElseThrow(() -> {
                    logger.error("Trainer not found with id: {}", id);
                    return new RuntimeException("Trainer not found with id: " + id);
                });
    }

    public Trainer findByUserUsername(String username) {
        return trainerRepository.findByUserUsername(username)
                .orElse(null);
    }

    @Transactional
    public Trainer saveTrainer(Trainer trainer) {
        logger.info("Saving trainer: {}", trainer);
        Trainer savedTrainer = trainerRepository.save(trainer);
        logger.info("Trainer saved successfully with ID: {}", savedTrainer.getId());
        return savedTrainer;
    }

    @Transactional
    public void deleteTrainer(Long id) {
        logger.info("Deleting trainer with ID: {}", id);
        trainerRepository.deleteById(id);
        logger.info("Trainer deleted successfully");
    }

    @Transactional
    public Trainer updateTrainer(Trainer trainer, MultipartFile photo) {
        if (photo != null && !photo.isEmpty()) {
            try {
                String filename = UUID.randomUUID().toString() + "_" + photo.getOriginalFilename();
                Path filePath = uploadDir.resolve(filename);
                Files.copy(photo.getInputStream(), filePath);
                trainer.setPhotoPath(filename);
            } catch (IOException e) {
                logger.error("Could not save photo", e);
                throw new RuntimeException("Could not save photo", e);
            }
        }
        return trainerRepository.save(trainer);
    }

    @Transactional
    public Trainer saveTrainerWithPhoto(Trainer trainer, MultipartFile photo) {
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
        return trainerRepository.save(trainer);
    }

    @Transactional
    public void addSkills(Long trainerId, String skills) {
        Trainer trainer = getTrainerById(trainerId);
        List<String> skillsList = Arrays.asList(skills.split("\\s*,\\s*"));
        trainer.setSkills(new ArrayList<>(skillsList));
        trainerRepository.save(trainer);
    }

    public Trainer findByUserId(Long userId) {
        return trainerRepository.findByUserId(userId);
    }
} 