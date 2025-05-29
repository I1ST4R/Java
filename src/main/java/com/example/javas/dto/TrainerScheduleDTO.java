package com.example.javas.dto;

import com.example.javas.models.TrainerSchedule;
import lombok.Data;

import java.time.LocalDate;
import java.util.List;
import java.util.stream.Collectors;

@Data
public class TrainerScheduleDTO {
    private Long id;
    private Long trainerId;
    private LocalDate date;
    private List<ScheduleIntervalDTO> intervals;
    
    public static TrainerScheduleDTO fromEntity(TrainerSchedule schedule) {
        TrainerScheduleDTO dto = new TrainerScheduleDTO();
        dto.setId(schedule.getId());
        dto.setTrainerId(schedule.getTrainer().getId());
        dto.setDate(schedule.getDate());
        dto.setIntervals(
            schedule.getIntervals().stream()
                .map(ScheduleIntervalDTO::fromEntity)
                .collect(Collectors.toList())
        );
        return dto;
    }
} 