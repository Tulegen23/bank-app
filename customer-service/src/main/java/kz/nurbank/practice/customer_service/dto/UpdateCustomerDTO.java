package kz.nurbank.practice.customer_service.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.*;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Schema(description = "Данные клиента для обновления")
public class UpdateCustomerDTO {

    @NotBlank(message = "ФИО не может быть пустым")
    @Schema(description = "Полное имя клиента", example = "Tulegen Nurbol Kanatuly")
    private String fullName;

    @NotBlank(message = "Номер телефона должен состоять из 11 цифр")
    @Size(min = 11, max = 11, message = "Номер телефона должен состоять ровно из 11 цифр")
    @Pattern(regexp = "^\\+?\\d{11}$", message = "Номер телефона должен быть в формате 87071234567 или 87071234567")
    @Schema(description = "Телефон клиента", example = "87071234567")
    private String phone;
}
