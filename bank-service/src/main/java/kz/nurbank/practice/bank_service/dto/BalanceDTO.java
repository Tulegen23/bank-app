package kz.nurbank.practice.bank_service.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import kz.nurbank.practice.bank_service.model.Currency;
import lombok.Data;

import java.math.BigDecimal;

@Data
@Schema(description = "Запрос для пополнения баланса счета")
public class BalanceDTO {
    @NotBlank(message = "Номер счета обязателен")
    @Schema(description = "Номер счета для пополнения", example = "KZ-8198490286970175")
    private String accountNumber;

    @NotNull(message = "Сумма обязательна")
    @DecimalMin(value = "1", message = "Сумма должна быть не меньше 1")
    @Schema(description = "Сумма пополнения", example = "1000.00")
    private BigDecimal amount;

    @NotNull(message = "Валюта обязательна")
    @Schema(description = "Валюта пополнения", example = "KZT")
    private Currency currency;
}