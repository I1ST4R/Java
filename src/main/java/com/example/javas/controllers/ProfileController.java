package com.example.javas.controllers;

import com.example.javas.models.Trainer;
import com.example.javas.models.Users;
import com.example.javas.service.TrainerService;
import com.example.javas.service.UserService;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.UUID;

@Controller
public class ProfileController {

    private final UserService userService;
    private final TrainerService trainerService;
    private final PasswordEncoder passwordEncoder;
    private final String UPLOAD_DIR = "uploads/";

    public ProfileController(UserService userService, TrainerService trainerService, PasswordEncoder passwordEncoder) {
        this.userService = userService;
        this.trainerService = trainerService;
        this.passwordEncoder = passwordEncoder;
    }

    @GetMapping("/profile")
    public String showProfile(Model model) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        String username = auth.getName();
        
        // Проверяем, является ли пользователь тренером
        boolean isTrainer = auth.getAuthorities().stream()
                .map(GrantedAuthority::getAuthority)
                .anyMatch(role -> role.equals("ROLE_TRAINER"));

        if (isTrainer) {
            Trainer trainer = trainerService.findByUserUsername(username);
            if (trainer == null) {
                throw new RuntimeException("Trainer not found");
            }
            model.addAttribute("trainer", trainer);
        } else {
        Users user = userService.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("User not found"));
            model.addAttribute("user", user);
        }
        
        return "profile";
    }

    @PostMapping("/profile/change-password")
    public String changePassword(
            @RequestParam String currentPassword,
            @RequestParam String newPassword,
            @RequestParam String confirmPassword,
            Model model) {
        
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        String username = auth.getName();
        Users user = userService.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("User not found"));

        // Verify current password
        if (!passwordEncoder.matches(currentPassword, user.getPassword())) {
            model.addAttribute("error", "Введенный пароль неверный");
            model.addAttribute("user", user);
            return "profile";
        }

        // Update password
        user.setPassword(passwordEncoder.encode(newPassword));
        userService.saveUser(user);
        
        model.addAttribute("message", "Пароль успешно изменен");
        model.addAttribute("user", user);
        return "profile";
    }

    @PostMapping("/profile/update")
    public String updateTrainerProfile(
            @ModelAttribute Trainer trainer,
            @RequestParam(value = "photo", required = false) MultipartFile photo,
            Model model) {
        
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        String username = auth.getName();
        
        // Проверяем, является ли пользователь тренером
        boolean isTrainer = auth.getAuthorities().stream()
                .map(GrantedAuthority::getAuthority)
                .anyMatch(role -> role.equals("ROLE_TRAINER"));

        if (!isTrainer) {
            model.addAttribute("error", "Доступ запрещен");
            return "profile";
        }

        Trainer existingTrainer = trainerService.findByUserUsername(username);
        if (existingTrainer == null) {
            throw new RuntimeException("Trainer not found");
        }

        // Обновляем данные тренера
        existingTrainer.setFirstName(trainer.getFirstName());
        existingTrainer.setLastName(trainer.getLastName());
        existingTrainer.setSpecialization(trainer.getSpecialization());
        existingTrainer.setEmail(trainer.getEmail());
        existingTrainer.setPhone(trainer.getPhone());
        existingTrainer.setHourlyRate(trainer.getHourlyRate());
        existingTrainer.setExperienceYears(trainer.getExperienceYears());
        existingTrainer.setDescription(trainer.getDescription());

        // Обработка загрузки фото
        if (photo != null && !photo.isEmpty()) {
            try {
                // Создаем директорию, если она не существует
                Path uploadPath = Paths.get(UPLOAD_DIR);
                if (!Files.exists(uploadPath)) {
                    Files.createDirectories(uploadPath);
                }

                // Генерируем уникальное имя файла
                String fileName = UUID.randomUUID().toString() + "_" + photo.getOriginalFilename();
                Path filePath = uploadPath.resolve(fileName);

                // Сохраняем файл
                Files.copy(photo.getInputStream(), filePath);

                // Обновляем путь к фото в профиле тренера
                existingTrainer.setPhotoPath(fileName);
            } catch (IOException e) {
                model.addAttribute("error", "Ошибка при загрузке фото: " + e.getMessage());
                model.addAttribute("trainer", existingTrainer);
                return "profile";
            }
        }

        trainerService.saveTrainer(existingTrainer);
        model.addAttribute("message", "Профиль успешно обновлен");
        model.addAttribute("trainer", existingTrainer);
        return "profile";
    }
} 