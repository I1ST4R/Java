package com.example.javas.service;

import com.example.javas.entity.Trainer;
import com.example.javas.repository.TrainerRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class TrainerService {
    private static final Logger logger = LoggerFactory.getLogger(TrainerService.class);
    private final TrainerRepository trainerRepository;

    @Autowired
    public TrainerService(TrainerRepository trainerRepository) {
        this.trainerRepository = trainerRepository;
        logger.info("TrainerService initialized with repository: {}", trainerRepository);
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
} 