package kz.nurbank.practice.customer_service.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
@Schema(description = "Запрос для аутентификации пользователя")
public class LoginRequest {

    @NotBlank(message = "Имя пользователя не может быть пустым")
    @Size(min = 4, max = 50, message = "Имя пользователя должно содержать от 4 до 50 символов")
    @Schema(description = "Имя пользователя", example = "admin")
    private String username;

    @NotBlank(message = "Пароль не может быть пустым")
    @Size(min = 6, max = 100, message = "Пароль должен содержать от 6 до 100 символов")
    @Schema(description = "Пароль пользователя", example = "admin123")
    private String password;
}
