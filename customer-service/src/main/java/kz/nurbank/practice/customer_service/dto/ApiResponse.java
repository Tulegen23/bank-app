package kz.nurbank.practice.customer_service.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

@Setter
@Getter
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

    public static <T> ApiResponse<T> success(String message, T data) {
        return new ApiResponse<>(200, message, data);
    }

    public static <T> ApiResponse<T> error(int status, String message) {
        return new ApiResponse<>(status, message, null);
    }
}
