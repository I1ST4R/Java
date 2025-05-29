package com.example.javas.service;

import com.example.javas.dto.ScheduleIntervalDTO;
import com.example.javas.dto.TrainerScheduleDTO;
import com.example.javas.models.ScheduleInterval;
import com.example.javas.models.Trainer;
import com.example.javas.models.TrainerSchedule;
import com.example.javas.models.Training;
import com.example.javas.repository.ScheduleIntervalRepository;
import com.example.javas.repository.TrainerRepository;
import com.example.javas.repository.TrainerScheduleRepository;
import com.example.javas.repository.TrainingRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class TrainerScheduleService {

    private final TrainerScheduleRepository scheduleRepository;
    private final ScheduleIntervalRepository intervalRepository;
    private final TrainerRepository trainerRepository;
    private final TrainingRepository trainingRepository;
    private final UserService userService;

    @Autowired
    public TrainerScheduleService(
            TrainerScheduleRepository scheduleRepository,
            ScheduleIntervalRepository intervalRepository,
            TrainerRepository trainerRepository,
            TrainingRepository trainingRepository,
            UserService userService) {
        this.scheduleRepository = scheduleRepository;
        this.intervalRepository = intervalRepository;
        this.trainerRepository = trainerRepository;
        this.trainingRepository = trainingRepository;
        this.userService = userService;
    }

    @Transactional
    public List<TrainerScheduleDTO> getTrainerScheduleForPeriod(Long trainerId, LocalDate startDate, LocalDate endDate) {
        Optional<Trainer> trainerOpt = trainerRepository.findById(trainerId);
        if (trainerOpt.isEmpty()) {
            throw new RuntimeException("Тренер не найден");
        }

        Trainer trainer = trainerOpt.get();
        List<TrainerSchedule> schedules = scheduleRepository.findByTrainerAndDateBetweenOrderByDate(
                trainer, startDate, endDate);

        // Создаем расписание для дней, которых еще нет в базе
        for (LocalDate tempDate = startDate; !tempDate.isAfter(endDate); tempDate = tempDate.plusDays(1)) {
            final LocalDate currentDate = tempDate; // Создаем final копию для использования в лямбда-выражении
            boolean hasSchedule = schedules.stream()
                    .anyMatch(s -> s.getDate().equals(currentDate));
            
            if (!hasSchedule) {
                TrainerSchedule newSchedule = new TrainerSchedule(trainer, currentDate);
                scheduleRepository.save(newSchedule);
                schedules.add(newSchedule);
            }
        }

        // Сортируем по дате
        schedules.sort((s1, s2) -> s1.getDate().compareTo(s2.getDate()));

        return schedules.stream()
                .map(TrainerScheduleDTO::fromEntity)
                .collect(Collectors.toList());
    }

    @Transactional
    public TrainerScheduleDTO getScheduleForDay(Long trainerId, LocalDate date) {
        Optional<Trainer> trainerOpt = trainerRepository.findById(trainerId);
        if (trainerOpt.isEmpty()) {
            throw new RuntimeException("Тренер не найден");
        }

        Trainer trainer = trainerOpt.get();
        TrainerSchedule schedule = scheduleRepository.findByTrainerAndDate(trainer, date);

        // Если для этого дня еще нет расписания, создаем новое
        if (schedule == null) {
            schedule = new TrainerSchedule(trainer, date);
            scheduleRepository.save(schedule);
        }

        return TrainerScheduleDTO.fromEntity(schedule);
    }

    @Transactional
    public ScheduleIntervalDTO addIntervalToSchedule(
            Long trainerId, 
            LocalDate date, 
            LocalTime startTime, 
            LocalTime endTime, 
            ScheduleInterval.Status status, 
            String description) {
        
        if (startTime.isAfter(endTime) || startTime.equals(endTime)) {
            throw new IllegalArgumentException("Начальное время должно быть раньше конечного времени");
        }
        
        if (startTime.isBefore(LocalTime.of(8, 0)) || endTime.isAfter(LocalTime.of(22, 0))) {
            throw new IllegalArgumentException("Интервал должен быть в рамках рабочего времени (8:00 - 22:00)");
        }

        TrainerScheduleDTO scheduleDTO = getScheduleForDay(trainerId, date);
        TrainerSchedule schedule = scheduleRepository.findById(scheduleDTO.getId())
                .orElseThrow(() -> new RuntimeException("Расписание не найдено"));

        // Проверяем на пересечение с существующими интервалами
        for (ScheduleIntervalDTO intervalDTO : scheduleDTO.getIntervals()) {
            if ((startTime.isBefore(intervalDTO.getEndTime()) && endTime.isAfter(intervalDTO.getStartTime())) ||
                startTime.equals(intervalDTO.getStartTime()) || endTime.equals(intervalDTO.getEndTime())) {
                throw new IllegalArgumentException("Интервал пересекается с существующим");
            }
        }

        ScheduleInterval interval = new ScheduleInterval(startTime, endTime, status);
        interval.setDescription(description);
        interval.setSchedule(schedule);
        
        intervalRepository.save(interval);
        schedule.addInterval(interval);
        scheduleRepository.save(schedule);

        return ScheduleIntervalDTO.fromEntity(interval);
    }

    @Transactional
    public ScheduleIntervalDTO updateInterval(
            Long intervalId, 
            ScheduleInterval.Status status, 
            String description) {
        
        ScheduleInterval interval = intervalRepository.findById(intervalId)
                .orElseThrow(() -> new RuntimeException("Интервал не найден"));

        // Тренер не может изменять статус BOOKED интервала
        if (interval.getStatus() == ScheduleInterval.Status.BOOKED) {
            throw new IllegalArgumentException("Невозможно изменить статус забронированного интервала.");
        }

        // Тренер не может сам установить статус BOOKED
        if (status == ScheduleInterval.Status.BOOKED) {
            throw new IllegalArgumentException("Тренеры не могут устанавливать статус BOOKED.");
        }
        
        interval.setStatus(status);
        interval.setDescription(description);
        
        intervalRepository.save(interval);
        
        return ScheduleIntervalDTO.fromEntity(interval);
    }

    @Transactional
    public ScheduleIntervalDTO bookInterval(Long intervalId, Long clientId) {
        ScheduleInterval interval = intervalRepository.findById(intervalId)
                .orElseThrow(() -> new RuntimeException("Interval not found with id: " + intervalId));

        if (interval.getStatus() != ScheduleInterval.Status.AVAILABLE) {
            throw new IllegalArgumentException("This time slot is no longer available for booking.");
        }

        // Создаем тренировку
        Training training = new Training();
        training.setClient(userService.findById(clientId)
                .orElseThrow(() -> new RuntimeException("Client not found")));
        training.setTrainer(interval.getSchedule().getTrainer());
        training.setDate(interval.getSchedule().getDate());
        training.setStartTime(interval.getStartTime());
        training.setEndTime(interval.getEndTime());
        training.setStatus(Training.TrainingStatus.UPCOMING);
        training.setDescription(interval.getDescription());
        
        // Сохраняем тренировку
        trainingRepository.save(training);

        // Обновляем статус интервала
        interval.setStatus(ScheduleInterval.Status.BOOKED);
        ScheduleInterval savedInterval = intervalRepository.save(interval);
        
        return ScheduleIntervalDTO.fromEntity(savedInterval);
    }

    @Transactional
    public void deleteInterval(Long intervalId) {
        ScheduleInterval interval = intervalRepository.findById(intervalId)
                .orElseThrow(() -> new RuntimeException("Интервал не найден"));
        
        TrainerSchedule schedule = interval.getSchedule();
        schedule.removeInterval(interval);
        
        intervalRepository.delete(interval);
        scheduleRepository.save(schedule);
    }
} 