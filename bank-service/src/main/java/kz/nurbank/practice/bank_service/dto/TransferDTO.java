package kz.nurbank.practice.bank_service.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import kz.nurbank.practice.bank_service.model.Currency;
import lombok.Data;

import java.math.BigDecimal;

@Data
@Schema(description = "Запрос для перевода денег")
public class TransferDTO {
    @NotBlank(message = "Обязательно номер счета отправителя")
    @Schema(description = "Номер счета отправителя", example = "KZ-8198490286970175")
    private String fromAccountNumber;

    @NotBlank(message = "Обязательно номер счета получателя")
    @Schema(description = "Номер счета получателя", example = "KZ-2600500094294534")
    private String toAccountNumber;

    @NotNull(message = "Сумма обязательно")
    @DecimalMin(value = "1", message = "Сумма должна быть не меньше 1")
    @Schema(description = "Сумма", example = "1000.00")
    private BigDecimal amount;

    @NotNull(message = "Обязательно валюта")
    @Schema(description = "Какая валюта", example = "KZT")
    private Currency currency;

    @Schema(description = "Наружный банк? (true - наружный, false - внутренный)", example = "false")
    public boolean isExternal = false;
}
