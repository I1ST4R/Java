package com.example.javas.repository;

import com.example.javas.models.Trainer;
import com.example.javas.models.TrainerSchedule;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;

@Repository
public interface TrainerScheduleRepository extends JpaRepository<TrainerSchedule, Long> {
    List<TrainerSchedule> findByTrainerAndDateBetweenOrderByDate(Trainer trainer, LocalDate startDate, LocalDate endDate);
    
    List<TrainerSchedule> findByTrainerOrderByDate(Trainer trainer);
    
    TrainerSchedule findByTrainerAndDate(Trainer trainer, LocalDate date);
    
    boolean existsByTrainerAndDate(Trainer trainer, LocalDate date);
} 