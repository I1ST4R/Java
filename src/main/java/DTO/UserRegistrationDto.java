package DTO;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class UserRegistrationDto {
    @NotBlank(message = "Имя пользователя обязательно")
    @Size(min = 4, max = 20, message = "Имя пользователя должно быть от 4 до 20 символов")
    private String username;

    @NotBlank(message = "Email обязателен")
    @Email(message = "Некорректный email")
    private String email;

    @NotBlank(message = "Пароль обязателен")
    @Size(min = 6, max = 40, message = "Пароль должен быть от 6 до 40 символов")
    private String password;
}