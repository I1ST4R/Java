package com.example.javas.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class UserRegistrationDto {
    @NotBlank(message = "Имя пользователя не может быть пустым")
    @Size(min = 3, max = 20, message = "Имя пользователя должно быть от 3 до 20 символов")
    private String username;

    @NotBlank(message = "Email не может быть пустым")
    @Size(max = 50, message = "Email не может быть длиннее 50 символов")
    @Email(message = "Введите корректный email адрес")
    private String email;

    @NotBlank(message = "Пароль не может быть пустым")
    @Size(min = 8, max = 40, message = "Пароль должен быть не менее 8 символов")
    private String password;
} 