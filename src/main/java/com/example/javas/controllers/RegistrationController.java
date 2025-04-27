package com.example.javas.controllers;

import com.example.javas.dto.UserRegistrationDto;
import com.example.javas.service.UserService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import com.example.javas.exception.UsernameAlreadyExistsException;
import com.example.javas.exception.EmailAlreadyExistsException;
import com.example.javas.exception.RoleNotFoundException;

@Controller
public class RegistrationController {

    private final UserService userService;

    public RegistrationController(UserService userService) {
        this.userService = userService;
    }

    @GetMapping("/register")
    public String showRegistrationForm(Model model) {
        model.addAttribute("user", new UserRegistrationDto());
        return "register";
    }

    @PostMapping("/register")
    public String registerUserAccount(@ModelAttribute("user") UserRegistrationDto registrationDto, 
                                    BindingResult bindingResult,
                                    Model model) {
        if (bindingResult.hasErrors()) {
            return "register";
        }

        try {
            userService.registerNewUser(registrationDto);
            return "redirect:/login?registered=true";
        } catch (UsernameAlreadyExistsException e) {
            model.addAttribute("error", "Username is already taken");
            return "register";
        } catch (EmailAlreadyExistsException e) {
            model.addAttribute("error", "Email is already in use");
            return "register";
        } catch (RoleNotFoundException e) {
            model.addAttribute("error", "System error: Role not found");
            return "register";
        } catch (Exception e) {
            model.addAttribute("error", "An error occurred during registration: " + e.getMessage());
            return "register";
        }
    }
} 