package com.example.javas.controllers;

import com.example.javas.dto.ScheduleIntervalDTO;
import com.example.javas.service.TrainerScheduleService;
import com.example.javas.service.UserService; // Понадобится для получения информации о клиенте
import com.example.javas.models.Users;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
// import org.springframework.security.access.prepost.PreAuthorize; // Если нужна будет авторизация
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import java.util.Map;

@Controller
@RequestMapping("/client") // Общий префикс для клиентских операций
public class BookingController {

    private static final Logger logger = LoggerFactory.getLogger(BookingController.class);
    private final TrainerScheduleService scheduleService;
    private final UserService userService; // Для информации о клиенте в будущем

    @Autowired
    public BookingController(TrainerScheduleService scheduleService, UserService userService) {
        this.scheduleService = scheduleService;
        this.userService = userService;
    }

    @PostMapping("/book/interval/{intervalId}")
    @ResponseBody
    // @PreAuthorize("isAuthenticated()") // Или @PreAuthorize("hasRole('ROLE_CLIENT')")
    public ResponseEntity<?> bookIntervalClient(@PathVariable Long intervalId, Authentication authentication) {
        if (authentication == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                                 .body(Map.of("message", "Пользователь не аутентифицирован."));
        }

        String username = authentication.getName();
        Users clientUser = userService.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("Пользователь не найден"));

        try {
            ScheduleIntervalDTO bookedInterval = scheduleService.bookInterval(intervalId, clientUser.getId());
            return ResponseEntity.ok(bookedInterval);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(Map.of("message", e.getMessage()));
        } catch (RuntimeException e) {
            logger.error("Error booking interval {} by client (User: {}): {}", 
                         intervalId, username, e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                                 .body(Map.of("message", "Не удалось забронировать тренировку. Пожалуйста, попробуйте позже."));
        }
    }
} 