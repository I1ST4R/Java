package com.example.javas.controllers;

import com.example.javas.dto.UserRegistrationDto;
import com.example.javas.models.Users;
import com.example.javas.service.UserService;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import com.example.javas.exception.UsernameAlreadyExistsException;
import com.example.javas.exception.EmailAlreadyExistsException;
import com.example.javas.exception.RoleNotFoundException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;
import jakarta.validation.Valid;

@Controller
public class RegistrationController {

    private final UserService userService;
    private final UserDetailsService userDetailsService;
    private final PasswordEncoder passwordEncoder;
    private final AuthenticationManagerBuilder authenticationManagerBuilder;

    public RegistrationController(UserService userService, 
                                UserDetailsService userDetailsService,
                                PasswordEncoder passwordEncoder,
                                AuthenticationManagerBuilder authenticationManagerBuilder) {
        this.userService = userService;
        this.userDetailsService = userDetailsService;
        this.passwordEncoder = passwordEncoder;
        this.authenticationManagerBuilder = authenticationManagerBuilder;
    }

    @GetMapping("/register")
    public String showRegistrationForm(Model model) {
        model.addAttribute("user", new UserRegistrationDto());
        return "register";
    }

    @PostMapping("/register")
    public String registerUserAccount(@Valid @ModelAttribute("user") UserRegistrationDto registrationDto, 
                                    BindingResult bindingResult,
                                    Model model) {
        if (bindingResult.hasErrors()) {
            return "register";
        }

        try {
            // Register the user
            Users savedUser = userService.registerNewUser(registrationDto);
            
            // Load user details
            UserDetails userDetails = userDetailsService.loadUserByUsername(registrationDto.getUsername());
            
            // Create authentication token
            UsernamePasswordAuthenticationToken authToken = new UsernamePasswordAuthenticationToken(
                userDetails,
                registrationDto.getPassword(),
                userDetails.getAuthorities()
            );
            
            // Authenticate the user
            AuthenticationManager authenticationManager = authenticationManagerBuilder.getObject();
            Authentication authentication = authenticationManager.authenticate(authToken);
            
            // Set the authentication in the security context
            SecurityContextHolder.getContext().setAuthentication(authentication);
            
            // Create a new session
            HttpServletRequest request = ((ServletRequestAttributes) RequestContextHolder.getRequestAttributes()).getRequest();
            HttpSession session = request.getSession(true);
            session.setAttribute("SPRING_SECURITY_CONTEXT", SecurityContextHolder.getContext());
            
            return "redirect:/?registered=true";
        } catch (UsernameAlreadyExistsException e) {
            model.addAttribute("error", "Имя пользователя уже занято");
            return "register";
        } catch (EmailAlreadyExistsException e) {
            model.addAttribute("error", "Email уже используется");
            return "register";
        } catch (RoleNotFoundException e) {
            model.addAttribute("error", "Ошибка при регистрации. Пожалуйста, попробуйте снова.");
            return "register";
        } catch (Exception e) {
            model.addAttribute("error", "Произошла ошибка при регистрации. Пожалуйста, попробуйте снова.");
            return "register";
        }
    }
} 