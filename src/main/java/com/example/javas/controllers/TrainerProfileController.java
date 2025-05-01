package com.example.javas.controllers;

import com.example.javas.models.Trainer;
import com.example.javas.models.Users;
import com.example.javas.service.TrainerService;
import com.example.javas.service.UserService;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.multipart.MultipartFile;

import java.util.ArrayList;
import java.util.List;

@Controller
public class TrainerProfileController {
    private static final Logger logger = LoggerFactory.getLogger(TrainerProfileController.class);
    private final TrainerService trainerService;
    private final UserService userService;
    private final PasswordEncoder passwordEncoder;
    private final UserDetailsService userDetailsService;

    public TrainerProfileController(TrainerService trainerService, UserService userService, 
                                  PasswordEncoder passwordEncoder, UserDetailsService userDetailsService) {
        this.trainerService = trainerService;
        this.userService = userService;
        this.passwordEncoder = passwordEncoder;
        this.userDetailsService = userDetailsService;
    }

    @GetMapping("/trainer/profile")
    public String showProfile(Model model) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        String username = auth.getName();
        
        Users user = userService.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("Пользователь не найден. Пожалуйста, войдите снова."));
        
        Trainer trainer = trainerService.findByUserId(user.getId());
        if (trainer == null) {
            throw new RuntimeException("Профиль тренера не найден. Пожалуйста, обратитесь к администратору.");
        }
        
        // Convert skills collection to a string
        String skillsString = String.join(", ", trainer.getSkills());
        model.addAttribute("trainer", trainer);
        model.addAttribute("skillsString", skillsString);
        return "trainers/profile";
    }

    @PostMapping("/trainer/profile/update")
    public String updateProfile(
            @RequestParam String username,
            @RequestParam(required = false) String password,
            @RequestParam String firstName,
            @RequestParam String lastName,
            @RequestParam String specialization,
            @RequestParam String email,
            @RequestParam String phone,
            @RequestParam double hourlyRate,
            @RequestParam int experienceYears,
            @RequestParam(required = false) String description,
            @RequestParam(required = false) MultipartFile photo,
            @RequestParam(required = false) String skills,
            Model model) {
        
        try {
            Authentication auth = SecurityContextHolder.getContext().getAuthentication();
            String currentUsername = auth.getName();
            
            Users user = userService.findByUsername(currentUsername)
                    .orElseThrow(() -> new RuntimeException("Пользователь не найден"));
            
            Trainer trainer = trainerService.findByUserId(user.getId());
            if (trainer == null) {
                throw new RuntimeException("Профиль тренера не найден");
            }

            // Check if email is being changed and if it already exists
            if (!email.equals(user.getEmail())) {
                if (userService.existsByEmail(email)) {
                    model.addAttribute("error", "Пользователь с таким email уже существует");
                    model.addAttribute("trainer", trainer);
                    model.addAttribute("skillsString", String.join(", ", trainer.getSkills()));
                    return "trainers/profile";
                }
            }
            
            // Update user credentials if changed
            boolean usernameChanged = !username.equals(currentUsername);
            if (usernameChanged) {
                user.setUsername(username);
            }
            if (password != null && !password.isEmpty()) {
                user.setPassword(passwordEncoder.encode(password));
            }
            user.setEmail(email);
            userService.saveUser(user);
            
            // Update trainer information
            trainer.setFirstName(firstName);
            trainer.setLastName(lastName);
            trainer.setSpecialization(specialization);
            trainer.setEmail(email);
            trainer.setPhone(phone);
            trainer.setHourlyRate(hourlyRate);
            trainer.setExperienceYears(experienceYears);
            trainer.setDescription(description);
            
            // Only update photo if a new one is provided
            if (photo != null && !photo.isEmpty()) {
                trainer = trainerService.saveTrainerWithPhoto(trainer, photo);
            } else {
                trainer = trainerService.saveTrainer(trainer);
            }
            
            if (skills != null && !skills.isEmpty()) {
                trainerService.addSkills(trainer.getId(), skills);
            }
            
            // If username was changed, update SecurityContext
            if (usernameChanged) {
                var userDetails = userDetailsService.loadUserByUsername(username);
                Authentication newAuth = new UsernamePasswordAuthenticationToken(
                    userDetails,
                    auth.getCredentials(),
                    auth.getAuthorities()
                );
                SecurityContextHolder.getContext().setAuthentication(newAuth);
            }
            
            return "redirect:/trainer/profile";
        } catch (Exception e) {
            logger.error("Ошибка при обновлении профиля", e);
            model.addAttribute("error", e.getMessage());
            
            // Ensure trainer object is in the model for error page
            Authentication auth = SecurityContextHolder.getContext().getAuthentication();
            String currentUsername = auth.getName();
            Users user = userService.findByUsername(currentUsername)
                    .orElseThrow(() -> new RuntimeException("Пользователь не найден"));
            Trainer trainer = trainerService.findByUserId(user.getId());
            if (trainer != null) {
                model.addAttribute("trainer", trainer);
                model.addAttribute("skillsString", String.join(", ", trainer.getSkills()));
            }
            
            return "trainers/profile";
        }
    }
} 