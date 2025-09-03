package kz.nurbank.practice.bank_service.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import jakarta.validation.Valid;
import kz.nurbank.practice.bank_service.aspect.LogAction;
import kz.nurbank.practice.bank_service.security.JwtUtils;
import kz.nurbank.practice.bank_service.dto.ApiResponse;
import kz.nurbank.practice.bank_service.dto.LoginRequest;
import kz.nurbank.practice.bank_service.dto.LoginResponse;
import kz.nurbank.practice.bank_service.service.AuditLogService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.AuthenticationException;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;

import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/login")
@RequiredArgsConstructor
public class LoginController {

    private final AuthenticationManager authManager;
    private final JwtUtils jwtUtils;
    private final AuditLogService auditLogService;

    @Operation(summary = "Аутентификация пользователя", description = "Аутентифицирует пользователя и возвращает JWT токен")
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Успешная аутентификация", content = @Content(schema = @Schema(implementation = LoginResponse.class))),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400", description = "Неверные данные", content = @Content(schema = @Schema(implementation = ApiResponse.class))),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "401", description = "Неверный логин или пароль", content = @Content(schema = @Schema(implementation = ApiResponse.class))),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "500", description = "Ошибка сервера", content = @Content(schema = @Schema(implementation = ApiResponse.class)))
    })
    @PostMapping
    @LogAction(action = "login")
    public ResponseEntity<?> login(@Valid @RequestBody LoginRequest request, BindingResult result) {
        String userId = request.getUsername() != null ? request.getUsername() : "Неизвестный";

        if (result.hasErrors()) {
            String errorMessage = result.getFieldErrors().stream()
                    .map(err -> err.getDefaultMessage())
                    .collect(Collectors.joining(", "));
            auditLogService.logAction(userId, "валидация", "ошибка: " + errorMessage);
            return ResponseEntity.badRequest().body(new ApiResponse<>(400, "Ошибка валидации: " + errorMessage, null));
        }

        try {
            authManager.authenticate(
                    new UsernamePasswordAuthenticationToken(request.getUsername(), request.getPassword())
            );
            String token = jwtUtils.generateToken(request.getUsername());
            return ResponseEntity.ok(new LoginResponse(token));
        } catch (AuthenticationException e) {
            auditLogService.logAction(userId, "аутентификация", "ошибка: Неверный логин или пароль");
            return ResponseEntity.status(401).body(new ApiResponse<>(401, "Неверный логин или пароль", null));
        } catch (Exception e) {
            auditLogService.logAction(userId, "Внутренняя ошибка", "ошибка: " + e.getMessage());
            return ResponseEntity.internalServerError().body(new ApiResponse<>(500, "Внутренняя ошибка: " + e.getMessage(), null));
        }
    }
}