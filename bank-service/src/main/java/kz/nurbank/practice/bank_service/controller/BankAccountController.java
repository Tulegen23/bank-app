package kz.nurbank.practice.bank_service.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.*;
import jakarta.validation.Valid;
import kz.nurbank.practice.bank_service.aspect.LogAction;
import kz.nurbank.practice.bank_service.dto.ApiResponse;
import kz.nurbank.practice.bank_service.dto.BalanceDTO;
import kz.nurbank.practice.bank_service.dto.BankAccountDTO;
import kz.nurbank.practice.bank_service.model.Currency;
import kz.nurbank.practice.bank_service.service.AuditLogService;
import kz.nurbank.practice.bank_service.service.BankAccountService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/accounts")
public class BankAccountController {

    private final BankAccountService service;
    private final AuditLogService auditLogService;

    public BankAccountController(BankAccountService service, AuditLogService auditLogService) {
        this.service = service;
        this.auditLogService = auditLogService;
    }

    private String getUserId() {
        try {
            return SecurityContextHolder.getContext().getAuthentication().getName();
        } catch (Exception e) {
            return "unknown";
        }
    }

    @Operation(
            summary = "Открыть счёт клиенту",
            description = "Открывает счет клиенту и сохраняет в базе данных"
    )
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Счёт успешно создан", content = @Content(schema = @Schema(implementation = ApiResponse.class))),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400", description = "Неверные данные или клиент не найден", content = @Content(schema = @Schema(implementation = ApiResponse.class))),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "500", description = "Ошибка сервера", content = @Content(schema = @Schema(implementation = ApiResponse.class)))
    })
    @PreAuthorize("hasRole('ADMIN') or @securityService.isOwnCustomer(#clientId)")
    @PostMapping
    @LogAction(action = "create account")
    public ResponseEntity<?> openAccount(
            @RequestParam Long clientId,
            @RequestParam Currency currency
    ) {
        String userId = getUserId();
        if (clientId == null || currency == null) {
            auditLogService.logAction(userId, "валидация", "ошибка: clientId и currency не могут быть пустым");
            return ResponseEntity.badRequest().body(new ApiResponse<>(400, "clientId и currency не могут быть пустым", null));
        }

        try {
            String accountNumber = service.createAccount(clientId, currency);
            return ResponseEntity.ok(new ApiResponse<>(200, "Счёт успешно создан", accountNumber));
        } catch (IllegalArgumentException e) {
            auditLogService.logAction(userId, "неправильный аргумент", "ошибка: " + e.getMessage());
            return ResponseEntity.badRequest().body(new ApiResponse<>(400, e.getMessage(), null));
        } catch (Exception e) {
            auditLogService.logAction(userId, "Внутренняя ошибка", "ошибка: " + e.getMessage());
            return ResponseEntity.internalServerError().body(new ApiResponse<>(500, "Внутренняя ошибка: " + e.getMessage(), null));
        }
    }

    @Operation(
            summary = "Получить счета клиента",
            description = "Возвращает счета клиента по указанному идентификатору. Если не найден — 404"
    )
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Счета найдены", content = @Content(schema = @Schema(implementation = ApiResponse.class))),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "Счета не найдены", content = @Content(schema = @Schema(implementation = ApiResponse.class))),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "500", description = "Ошибка сервера", content = @Content(schema = @Schema(implementation = ApiResponse.class)))
    })
    @PreAuthorize("hasRole('ADMIN') or @securityService.isOwnCustomer(#clientId)")
    @GetMapping("/{clientId}")
    @LogAction(action = "get accounts")
    public ResponseEntity<?> getAccounts(@PathVariable Long clientId) {
        String userId = getUserId();
        if (clientId == null) {
            auditLogService.logAction(userId, "валидация", "ошибка: clientId не может быть пустым");
            return ResponseEntity.badRequest().body(new ApiResponse<>(400, "clientId не может быть пустым", null));
        }

        try {
            List<BankAccountDTO> accounts = service.getAccountsByClientId(clientId);
            if (accounts.isEmpty()) {
                return ResponseEntity.status(404).body(new ApiResponse<>(404, "Счета для клиента " + clientId + " не найдены", null));
            }
            return ResponseEntity.ok(new ApiResponse<>(200, "Счета найдены", accounts));
        } catch (Exception e) {
            auditLogService.logAction(userId, "Внутренняя ошибка", "ошибка: " + e.getMessage());
            return ResponseEntity.internalServerError().body(new ApiResponse<>(500, "Внутренняя ошибка: " + e.getMessage(), null));
        }
    }

    @Operation(
            summary = "Пополнить баланс счета",
            description = "Пополняет баланс указанного счета с учетом комиссии"
    )
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Баланс пополнен", content = @Content(schema = @Schema(implementation = ApiResponse.class))),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "Неверные данные или счет не найден", content = @Content(schema = @Schema(implementation = ApiResponse.class))),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "500", description = "Ошибка сервера", content = @Content(schema = @Schema(implementation = ApiResponse.class)))
    })
    @PreAuthorize("hasRole('ADMIN') or @securityService.isOwnCustomer(#clientId)")
    @PostMapping("/balance")
    @LogAction(action = "balance")
    public ResponseEntity<?> balance(@Valid @RequestBody BalanceDTO balanceDTO, BindingResult result) {
        String userId = getUserId();
        if (result.hasErrors()) {
            String errorMessage = result.getFieldErrors().stream()
                    .map(err -> err.getDefaultMessage())
                    .collect(Collectors.joining(", "));
            auditLogService.logAction(userId, "валидация", "ошибка: " + errorMessage);
            return ResponseEntity.badRequest().body(new ApiResponse<>(400, "Ошибка валидации: " + errorMessage, null));
        }

        try {
            service.balance(balanceDTO);
            return ResponseEntity.ok(new ApiResponse<>(200, "Баланс успешно пополнен", balanceDTO.getAccountNumber()));
        } catch (IllegalArgumentException e) {
            auditLogService.logAction(userId, "неправильный аргумент", "ошибка: " + e.getMessage());
            return ResponseEntity.badRequest().body(new ApiResponse<>(400, e.getMessage(), null));
        } catch (Exception e) {
            auditLogService.logAction(userId, "Внутренняя ошибка", "ошибка: " + e.getMessage());
            return ResponseEntity.internalServerError().body(new ApiResponse<>(500, "Внутренняя ошибка: " + e.getMessage(), null));
        }
    }
}