package com.example.javas.dto;

import com.example.javas.models.ScheduleInterval;
import lombok.Data;

import java.time.LocalTime;

@Data
public class ScheduleIntervalDTO {
    private Long id;
    private LocalTime startTime;
    private LocalTime endTime;
    private ScheduleInterval.Status status;
    private String description;
    
    public static ScheduleIntervalDTO fromEntity(ScheduleInterval interval) {
        ScheduleIntervalDTO dto = new ScheduleIntervalDTO();
        dto.setId(interval.getId());
        dto.setStartTime(interval.getStartTime());
        dto.setEndTime(interval.getEndTime());
        dto.setStatus(interval.getStatus());
        dto.setDescription(interval.getDescription());
        return dto;
    }
} 