package kz.nurbank.practice.customer_service.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
@Schema(description = "Данные для создания клиента и пользователя")
public class RegistrationDTO {

    @NotBlank(message = "ФИО не может быть пустым")
    @Schema(description = "Полное имя клиента", example = "Tulegen Nurbol Kanatuly")
    private String fullName;

    @NotBlank(message = "ИИН должен состоять ровно из 12 цифр")
    @Size(min = 12, max = 12, message = "ИИН должен состоять ровно из 12 цифр")
    @Pattern(regexp = "\\d{12}", message = "ИИН должен состоять только из цифр")
    @Schema(description = "ИИН клиента", example = "010101123456")
    private String iin;

    @NotBlank(message = "Номер телефона должен состоять из 11 цифр")
    @Size(min = 11, max = 11, message = "Номер телефона должен состоять ровно из 11 цифр")
    @Pattern(regexp = "^\\+?\\d{11}$", message = "Номер телефона должен быть в формате 87071234567")
    @Schema(description = "Телефон клиента", example = "87071234567")
    private String phone;

    @NotBlank(message = "Имя пользователя не может быть пустым")
    @Schema(description = "Имя пользователя для логина", example = "tulegen")
    private String username;

    @NotBlank(message = "Пароль не может быть пустым")
    @Size(min = 6, message = "Пароль должен быть не менее 6 символов")
    @Schema(description = "Пароль", example = "password123")
    private String password;
}