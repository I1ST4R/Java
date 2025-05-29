package com.example.javas.models;

import jakarta.persistence.*;
import java.time.LocalTime;

@Entity
@Table(name = "schedule_intervals")
public class ScheduleInterval {
    
    public enum Status {
        AVAILABLE, // Зеленый - доступен для тренировок
        UNAVAILABLE, // Красный - недоступен
        UNDEFINED, // Желтый - не определено
        BOOKED // Оранжевый - забронирован клиентом
    }
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @ManyToOne
    @JoinColumn(name = "schedule_id", nullable = false)
    private TrainerSchedule schedule;
    
    @Column(nullable = false)
    private LocalTime startTime;
    
    @Column(nullable = false)
    private LocalTime endTime;
    
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private Status status = Status.UNDEFINED;
    
    @Column(columnDefinition = "TEXT")
    private String description;
    
    // Constructors
    public ScheduleInterval() {
    }
    
    public ScheduleInterval(LocalTime startTime, LocalTime endTime, Status status) {
        this.startTime = startTime;
        this.endTime = endTime;
        this.status = status;
    }
    
    // Getters and Setters
    public Long getId() {
        return id;
    }
    
    public void setId(Long id) {
        this.id = id;
    }
    
    public TrainerSchedule getSchedule() {
        return schedule;
    }
    
    public void setSchedule(TrainerSchedule schedule) {
        this.schedule = schedule;
    }
    
    public LocalTime getStartTime() {
        return startTime;
    }
    
    public void setStartTime(LocalTime startTime) {
        this.startTime = startTime;
    }
    
    public LocalTime getEndTime() {
        return endTime;
    }
    
    public void setEndTime(LocalTime endTime) {
        this.endTime = endTime;
    }
    
    public Status getStatus() {
        return status;
    }
    
    public void setStatus(Status status) {
        this.status = status;
    }
    
    public String getDescription() {
        return description;
    }
    
    public void setDescription(String description) {
        this.description = description;
    }
} 