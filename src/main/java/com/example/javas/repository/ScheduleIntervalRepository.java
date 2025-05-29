package com.example.javas.repository;

import com.example.javas.models.ScheduleInterval;
import com.example.javas.models.TrainerSchedule;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ScheduleIntervalRepository extends JpaRepository<ScheduleInterval, Long> {
    List<ScheduleInterval> findByScheduleOrderByStartTime(TrainerSchedule schedule);
} 