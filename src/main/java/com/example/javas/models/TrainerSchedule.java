package com.example.javas.models;

import jakarta.persistence.*;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "trainer_schedules")
public class TrainerSchedule {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @ManyToOne
    @JoinColumn(name = "trainer_id", nullable = false)
    private Trainer trainer;
    
    @Column(nullable = false)
    private LocalDate date;
    
    @OneToMany(mappedBy = "schedule", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<ScheduleInterval> intervals = new ArrayList<>();
    
    // Constructors
    public TrainerSchedule() {
    }
    
    public TrainerSchedule(Trainer trainer, LocalDate date) {
        this.trainer = trainer;
        this.date = date;
    }
    
    // Getters and Setters
    public Long getId() {
        return id;
    }
    
    public void setId(Long id) {
        this.id = id;
    }
    
    public Trainer getTrainer() {
        return trainer;
    }
    
    public void setTrainer(Trainer trainer) {
        this.trainer = trainer;
    }
    
    public LocalDate getDate() {
        return date;
    }
    
    public void setDate(LocalDate date) {
        this.date = date;
    }
    
    public List<ScheduleInterval> getIntervals() {
        return intervals;
    }
    
    public void setIntervals(List<ScheduleInterval> intervals) {
        this.intervals = intervals;
    }
    
    // Helper methods
    public void addInterval(ScheduleInterval interval) {
        intervals.add(interval);
        interval.setSchedule(this);
    }
    
    public void removeInterval(ScheduleInterval interval) {
        intervals.remove(interval);
        interval.setSchedule(null);
    }
} 