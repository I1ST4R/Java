package com.example.javas.controllers;

import com.example.javas.dto.ScheduleIntervalDTO;
import com.example.javas.dto.TrainerScheduleDTO;
import com.example.javas.models.ScheduleInterval;
import com.example.javas.models.Trainer;
import com.example.javas.repository.TrainerRepository;
import com.example.javas.service.TrainerScheduleService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Map;

@Controller
@RequestMapping("/trainer/schedule")
@PreAuthorize("hasRole('TRAINER')")
public class TrainerScheduleController {

    private final TrainerScheduleService scheduleService;
    private final TrainerRepository trainerRepository;

    @Autowired
    public TrainerScheduleController(TrainerScheduleService scheduleService, TrainerRepository trainerRepository) {
        this.scheduleService = scheduleService;
        this.trainerRepository = trainerRepository;
    }

    @GetMapping
    public String showSchedulePage(Model model, Authentication authentication) {
        String username = authentication.getName();
        Trainer trainer = trainerRepository.findByUserUsername(username)
                .orElseThrow(() -> new RuntimeException("Тренер не найден"));
        
        // Получаем даты на две недели
        LocalDate startDate = LocalDate.now();
        LocalDate endDate = startDate.plusDays(13); // 14 дней включая сегодняшний
        
        List<TrainerScheduleDTO> schedules = scheduleService.getTrainerScheduleForPeriod(
                trainer.getId(), startDate, endDate);
        
        model.addAttribute("schedules", schedules);
        model.addAttribute("startDate", startDate);
        model.addAttribute("endDate", endDate);
        model.addAttribute("trainer", trainer);
        model.addAttribute("today", LocalDate.now());
        
        return "trainer/schedule";
    }
    
    @PostMapping("/interval/add")
    @ResponseBody
    public ResponseEntity<?> addInterval(
            @RequestParam("trainerId") Long trainerId,
            @RequestParam("date") @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date,
            @RequestParam("startTime") @DateTimeFormat(iso = DateTimeFormat.ISO.TIME) LocalTime startTime,
            @RequestParam("endTime") @DateTimeFormat(iso = DateTimeFormat.ISO.TIME) LocalTime endTime,
            @RequestParam("status") String status,
            @RequestParam(value = "description", required = false) String description) {
        
        try {
            ScheduleInterval.Status intervalStatus = ScheduleInterval.Status.valueOf(status);
            ScheduleIntervalDTO interval = scheduleService.addIntervalToSchedule(
                    trainerId, date, startTime, endTime, intervalStatus, description);
            return ResponseEntity.ok(interval);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("error", "Не удалось добавить интервал: " + e.getMessage()));
        }
    }
    
    @PostMapping("/interval/update/{id}")
    @ResponseBody
    public ResponseEntity<?> updateInterval(
            @PathVariable("id") Long intervalId,
            @RequestParam("status") String status,
            @RequestParam(value = "description", required = false) String description) {
        
        try {
            ScheduleInterval.Status intervalStatus = ScheduleInterval.Status.valueOf(status);
            ScheduleIntervalDTO interval = scheduleService.updateInterval(intervalId, intervalStatus, description);
            return ResponseEntity.ok(interval);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("error", "Не удалось обновить интервал: " + e.getMessage()));
        }
    }
    
    @DeleteMapping("/interval/{id}")
    @ResponseBody
    public ResponseEntity<?> deleteInterval(@PathVariable("id") Long intervalId) {
        try {
            scheduleService.deleteInterval(intervalId);
            return ResponseEntity.ok(Map.of("success", true));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("error", "Не удалось удалить интервал: " + e.getMessage()));
        }
    }
} 