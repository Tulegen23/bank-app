package kz.nurbank.practice.bank_service.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
@Schema(description = "Код ответа")
public class ApiResponse<T> {
    @NotBlank
    @Schema(description = "Статус")
    private int status;
    @NotBlank
    @Schema(description = "Сообщения")
    private String message;
    @NotBlank
    @Schema(description = "Обьект")
    private T data;
}
