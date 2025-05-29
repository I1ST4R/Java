package com.example.javas.controllers;

import com.example.javas.service.TrainerService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class AboutController {

    private final TrainerService trainerService;

    public AboutController(TrainerService trainerService) {
        this.trainerService = trainerService;
    }

    @GetMapping("/about")
    public String showAboutPage(Model model) {
        model.addAttribute("trainers", trainerService.getAllTrainers());
        return "about";
    }
} 