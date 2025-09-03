package kz.nurbank.practice.bank_service.dto;

import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import kz.nurbank.practice.bank_service.model.Currency;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import lombok.*;

@Getter
@Setter
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Schema(description = "Счета клиента")
public class BankAccountDTO {

    @NotBlank(message = "Номер счета не должен быть пустым")
    @Pattern(regexp = "^KZ-\\d{16}$", message = "Номер счета должен быть в формате KZ-xxxxxxxxxxxxxxxx")
    @Schema(description = "Номер счета", example = "KZ-1234123412341234")
    private String accountNumber;

    @NotNull(message = "ID клиента не должен быть пустым")
    @Schema(description = "ID клиента", example = "1")
    private Long clientId;

    @NotNull(message = "Валюта обязательна")
    @Enumerated(EnumType.STRING)
    @Pattern(regexp = "KZT|USD|EUR", message = "Разрешены только KZT, USD, EUR")
    @Schema(description = "Валюта счета", example = "KZT")
    private Currency currency;
}
